package cn.idealer01.trigger.http;

import cn.idealer.wrench.rate.limiter.types.annotations.RateLimiterAccessInterpector;
import cn.idealer01.api.IMarketIndexService;
import cn.idealer01.api.dto.GoodsMarketRequestDTO;
import cn.idealer01.api.dto.GoodsMarketResponseDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.domain.activity.model.entity.MarketProductEntity;
import cn.idealer01.domain.activity.model.entity.TrialBalanceEntity;
import cn.idealer01.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import cn.idealer01.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.idealer01.domain.activity.model.valobj.TeamStatisticVO;
import cn.idealer01.domain.activity.service.IIndexGroupBuyMarketService;
import cn.idealer01.types.enums.ResponseCode;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@CrossOrigin("*")
@Slf4j
@RequestMapping("/api/v1/gbm/index/")
public class MarketIndexController implements IMarketIndexService {
    @Resource
    private IIndexGroupBuyMarketService indexGroupBuyMarketService;
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
            GoodsMarketResponseDTO.Goods goods = GoodsMarketResponseDTO.Goods.builder()
                    .goodsId(trialBalanceEntity.getGoodsId())
                    .originalPrice(trialBalanceEntity.getOriginalPrice())
                    .deductionPrice(trialBalanceEntity.getDeductionPrice())
                    .payPrice(trialBalanceEntity.getPayPrice())
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
                            .build())
                    .build();

            log.info("查询拼团营销配置完成{}, goodsId:{}, response:{}", goodsMarketRequestDTO.getUserId(), goodsMarketRequestDTO.getGoodsId(), JSON.toJSONString(response));

            return response;

        }catch (Exception e){
            log.error("查询拼团营销配置失败:{} goodsId:{}", goodsMarketRequestDTO.getUserId(), goodsMarketRequestDTO.getGoodsId(), e);
            return Response.<GoodsMarketResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }

    }

    public Response<GoodsMarketResponseDTO> queryGroupBuyMarketConfigFallback(@RequestBody GoodsMarketRequestDTO requestDTO){
        log.error("查询拼团营销配置限流:{}", requestDTO.getUserId());
        return Response.<GoodsMarketResponseDTO>builder()
                .code(ResponseCode.RATE_LIMITER.getCode())
                .info(ResponseCode.RATE_LIMITER.getInfo())
                .build();
    }
}
