package cn.idealer01.trigger.http;

import cn.idealer.wrench.rate.limiter.types.annotations.RateLimiterAccessInterpector;
import cn.idealer01.api.IMarketIndexService;
import cn.idealer01.api.dto.GoodsMarketRequestDTO;
import cn.idealer01.api.dto.GoodsMarketResponseDTO;
import cn.idealer01.api.dto.SkuListResponseDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.domain.activity.model.entity.MarketProductEntity;
import cn.idealer01.domain.activity.model.entity.TrialBalanceEntity;
import cn.idealer01.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import cn.idealer01.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.idealer01.domain.activity.model.valobj.TeamStatisticVO;
import cn.idealer01.domain.activity.service.IIndexGroupBuyMarketService;
import cn.idealer01.infrastructure.dao.ISkuDao;
import cn.idealer01.infrastructure.dao.ISkuImageDao;
import cn.idealer01.infrastructure.dao.po.Sku;
import cn.idealer01.infrastructure.dao.po.SkuImage;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin("*")
@Slf4j
@RequestMapping("/api/v1/gbm/index/")
public class MarketIndexController implements IMarketIndexService {
    @Resource
    private IIndexGroupBuyMarketService indexGroupBuyMarketService;
    @Resource
    private ISkuDao skuDao;
    @Resource
    private ISkuImageDao skuImageDao;

    public MarketIndexController() {
    }

    public MarketIndexController(IIndexGroupBuyMarketService indexGroupBuyMarketService, ISkuDao skuDao) {
        this(indexGroupBuyMarketService, skuDao, null);
    }

    public MarketIndexController(IIndexGroupBuyMarketService indexGroupBuyMarketService, ISkuDao skuDao, ISkuImageDao skuImageDao) {
        this.indexGroupBuyMarketService = indexGroupBuyMarketService;
        this.skuDao = skuDao;
        this.skuImageDao = skuImageDao;
    }
    @Override
    @PostMapping("query_group_buy_market_config")
    @RateLimiterAccessInterpector(key = "userId", permitsPerSecond = 1L, blacklistCount = 1L, fallbackMethod="queryGroupBuyMarketConfigFallback")
    public Response<GoodsMarketResponseDTO> queryGroupBuyMarketConfig(@RequestBody GoodsMarketRequestDTO goodsMarketRequestDTO) {
        try{
            log.info("查询拼团营销配置开始:{} goodsId:{}", goodsMarketRequestDTO.getUserId(),goodsMarketRequestDTO.getGoodsId());
            //检查是否为空
            if(StringUtils.isBlank(goodsMarketRequestDTO.getUserId()) || StringUtils.isBlank(goodsMarketRequestDTO.getSource()) || StringUtils.isBlank(goodsMarketRequestDTO.getChannel()) || StringUtils.isBlank(goodsMarketRequestDTO.getGoodsId())){
                return Response.<GoodsMarketResponseDTO>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }

            //1.优惠折扣计算
            TrialBalanceEntity trialBalanceEntity = indexGroupBuyMarketService.indexMarketTrial(MarketProductEntity.builder()
                    .userId(goodsMarketRequestDTO.getUserId())
                    .source(goodsMarketRequestDTO.getSource())
                    .channel(goodsMarketRequestDTO.getChannel())
                    .goodsId(goodsMarketRequestDTO.getGoodsId())
                    .build());

            GroupBuyActivityDiscountVO groupBuyActivityDiscountVO = trialBalanceEntity.getGroupBuyActivityDiscountVO();
            Long activityId = groupBuyActivityDiscountVO.getActivityId();

            //2.查询拼团组队
            List<UserGroupBuyOrderDetailEntity> userGroupBuyOrderDetailEntityList = indexGroupBuyMarketService.queryInProgressUserGroupBuyOrderDetailList(activityId, goodsMarketRequestDTO.getUserId(), 1, 2);

            //3.统计拼团数据
            TeamStatisticVO teamStatisticVO = indexGroupBuyMarketService.queryTeamStatisticByActivity(activityId);

            //4.将查询到的数据进行封装
            //4.1封装商品数据
            Sku sku = skuDao.querySkuByGoodsId(trialBalanceEntity.getGoodsId());
            List<String> imageUrls = queryImageUrls(trialBalanceEntity.getGoodsId());
            GoodsMarketResponseDTO.Goods goods = GoodsMarketResponseDTO.Goods.builder()
                    .goodsId(trialBalanceEntity.getGoodsId())
                    .goodsName(sku == null ? null : sku.getGoodsName())
                    .originalPrice(trialBalanceEntity.getOriginalPrice())
                    .deductionPrice(trialBalanceEntity.getDeductionPrice())
                    .payPrice(trialBalanceEntity.getPayPrice())
                    .coverImageUrl(firstImageUrl(imageUrls))
                    .imageUrls(imageUrls)
                    .build();

            //4.2 封装拼团详细信息
            List<GoodsMarketResponseDTO.Team> teamList = new ArrayList<>();
            if(null != userGroupBuyOrderDetailEntityList && !userGroupBuyOrderDetailEntityList.isEmpty()) {
                for (UserGroupBuyOrderDetailEntity userGroupBuyOrderDetailEntity : userGroupBuyOrderDetailEntityList) {
                    GoodsMarketResponseDTO.Team team = GoodsMarketResponseDTO.Team.builder()
                            .userId(userGroupBuyOrderDetailEntity.getUserId())
                            .teamId(userGroupBuyOrderDetailEntity.getTeamId())
                            .activityId(userGroupBuyOrderDetailEntity.getActivityId())
                            .targetCount(userGroupBuyOrderDetailEntity.getTargetCount())
                            .lockCount(userGroupBuyOrderDetailEntity.getLockCount())
                            .completeCount(userGroupBuyOrderDetailEntity.getCompleteCount())
                            .outTradeNo(userGroupBuyOrderDetailEntity.getOutTradeNo())
                            .validStartTime(userGroupBuyOrderDetailEntity.getValidStartTime())
                            .validEndTime(userGroupBuyOrderDetailEntity.getValidEndTime())
                            //拼团剩余时间
                            .validTimeCountdown(GoodsMarketResponseDTO.Team.differenceDateTime2Str(new Date(), userGroupBuyOrderDetailEntity.getValidEndTime()))
                            .build();

                    teamList.add(team);
                }
            }

            //4.3 封装拼团统计数据
            GoodsMarketResponseDTO.TeamStatistic teamStatistic = GoodsMarketResponseDTO.TeamStatistic.builder()
                    .allTeamCount(teamStatisticVO.getAllTeamCount())
                    .allTeamCompleteCount(teamStatisticVO.getAllTeamCompleteCount())
                    .allTeamUserCount(teamStatisticVO.getAllTeamUserCount())
                    .build();

            Response<GoodsMarketResponseDTO> response = Response.<GoodsMarketResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(GoodsMarketResponseDTO.builder()
                            .activityId(activityId)
                            .goods(goods)
                            .teamList(teamList)
                            .teamStatistic(teamStatistic)
                            .isVisible(trialBalanceEntity.getIsVisible())
                            .isEnable(trialBalanceEntity.getIsEnable())
                            .build())
                    .build();

            log.info("查询拼团营销配置完成{}, goodsId:{}, response:{}", goodsMarketRequestDTO.getUserId(), goodsMarketRequestDTO.getGoodsId(), JSON.toJSONString(response));

            return response;

        } catch (AppException e) {
            log.warn("查询拼团营销配置业务返回:{} goodsId:{} code:{} info:{}", goodsMarketRequestDTO.getUserId(), goodsMarketRequestDTO.getGoodsId(), e.getCode(), e.getInfo());
            if (ResponseCode.E0002.getCode().equals(e.getCode())) {
                return queryPlainGoodsMarketConfig(goodsMarketRequestDTO);
            }
            return Response.<GoodsMarketResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e){
            log.error("查询拼团营销配置失败:{} goodsId:{}", goodsMarketRequestDTO.getUserId(), goodsMarketRequestDTO.getGoodsId(), e);
            return Response.<GoodsMarketResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }

    }

    private Response<GoodsMarketResponseDTO> queryPlainGoodsMarketConfig(GoodsMarketRequestDTO requestDTO) {
        Sku sku = skuDao.querySkuByGoodsId(requestDTO.getGoodsId());
        if (null == sku || (null != sku.getStatus() && sku.getStatus() != 0)) {
            return Response.<GoodsMarketResponseDTO>builder()
                    .code(ResponseCode.E0002.getCode())
                    .info(ResponseCode.E0002.getInfo())
                    .build();
        }

        List<String> imageUrls = queryImageUrls(sku.getGoodsId());
        GoodsMarketResponseDTO.Goods goods = GoodsMarketResponseDTO.Goods.builder()
                .goodsId(sku.getGoodsId())
                .goodsName(sku.getGoodsName())
                .originalPrice(sku.getOriginalPrice())
                .deductionPrice(BigDecimal.ZERO)
                .payPrice(sku.getOriginalPrice())
                .coverImageUrl(firstImageUrl(imageUrls))
                .imageUrls(imageUrls)
                .build();

        GoodsMarketResponseDTO.TeamStatistic teamStatistic = GoodsMarketResponseDTO.TeamStatistic.builder()
                .allTeamCount(0)
                .allTeamCompleteCount(0)
                .allTeamUserCount(0)
                .build();

        return Response.<GoodsMarketResponseDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(GoodsMarketResponseDTO.builder()
                        .activityId(null)
                        .goods(goods)
                        .teamList(Collections.emptyList())
                        .teamStatistic(teamStatistic)
                        .isVisible(true)
                        .isEnable(true)
                        .build())
                .build();
    }

    public Response<GoodsMarketResponseDTO> queryGroupBuyMarketConfigFallback(@RequestBody GoodsMarketRequestDTO requestDTO){
        log.error("查询拼团营销配置限流:{}", requestDTO.getUserId());
        return Response.<GoodsMarketResponseDTO>builder()
                .code(ResponseCode.RATE_LIMITER.getCode())
                .info(ResponseCode.RATE_LIMITER.getInfo())
                .build();
    }

    @GetMapping("query_sku_list")
    public Response<SkuListResponseDTO> querySkuList() {
        try {
            List<Sku> skuList = skuDao.querySkuList();
            List<String> goodsIds = skuList.stream().map(Sku::getGoodsId).collect(Collectors.toList());
            List<SkuImage> imageList = queryImagesByGoodsIds(goodsIds);
            Map<String, List<SkuImage>> imagesByGoodsId = imageList.stream().collect(Collectors.groupingBy(SkuImage::getGoodsId));
            List<SkuListResponseDTO.SkuItem> skuItems = new ArrayList<>();
            for (Sku sku : skuList) {
                if (sku.getStatus() != null && sku.getStatus() != 0) {
                    continue;
                }
                List<SkuImage> images = imagesByGoodsId.getOrDefault(sku.getGoodsId(), Collections.emptyList());

                skuItems.add(SkuListResponseDTO.SkuItem.builder()
                        .goodsId(sku.getGoodsId())
                        .goodsName(sku.getGoodsName())
                        .originalPrice(sku.getOriginalPrice())
                        .coverImageUrl(images.isEmpty() ? null : images.get(0).getImageUrl())
                        .build());
            }
            return Response.<SkuListResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(SkuListResponseDTO.builder().skuList(skuItems).build())
                    .build();
        } catch (Exception e) {
            log.error("查询商品列表失败", e);
            return Response.<SkuListResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    private List<String> queryImageUrls(String goodsId) {
        if (skuImageDao == null) {
            return Collections.emptyList();
        }
        List<SkuImage> images;
        try {
            images = skuImageDao.querySkuImagesByGoodsId(goodsId);
        } catch (BadSqlGrammarException e) {
            log.warn("商品图片表不可用，按无图片处理 goodsId:{} error:{}", goodsId, e.getMessage());
            return Collections.emptyList();
        }
        if (images == null || images.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> imageUrls = new ArrayList<>();
        for (SkuImage image : images) {
            imageUrls.add(image.getImageUrl());
        }
        return imageUrls;
    }

    private List<SkuImage> queryImagesByGoodsIds(List<String> goodsIds) {
        if (skuImageDao == null || goodsIds.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return skuImageDao.querySkuImagesByGoodsIds(goodsIds);
        } catch (BadSqlGrammarException e) {
            log.warn("商品图片表不可用，商品列表按无图片处理 goodsIds:{} error:{}", goodsIds, e.getMessage());
            return Collections.emptyList();
        }
    }

    private String firstImageUrl(List<String> imageUrls) {
        return imageUrls == null || imageUrls.isEmpty() ? null : imageUrls.get(0);
    }
}
