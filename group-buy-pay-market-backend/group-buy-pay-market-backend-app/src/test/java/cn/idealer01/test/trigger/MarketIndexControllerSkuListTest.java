package cn.idealer01.test.trigger;

import cn.idealer01.api.dto.GoodsMarketRequestDTO;
import cn.idealer01.api.dto.GoodsMarketResponseDTO;
import cn.idealer01.api.dto.SkuListResponseDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.domain.activity.model.entity.TrialBalanceEntity;
import cn.idealer01.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.idealer01.domain.activity.model.valobj.TeamStatisticVO;
import cn.idealer01.domain.activity.service.IIndexGroupBuyMarketService;
import cn.idealer01.infrastructure.dao.ICrowdTagsDao;
import cn.idealer01.infrastructure.dao.IGroupBuyActivityDao;
import cn.idealer01.infrastructure.dao.IGroupBuyDiscountDao;
import cn.idealer01.infrastructure.dao.ISCSkuActivityDao;
import cn.idealer01.infrastructure.dao.ISkuDao;
import cn.idealer01.infrastructure.dao.ISkuImageDao;
import cn.idealer01.infrastructure.dao.po.CrowdTags;
import cn.idealer01.infrastructure.dao.po.GroupBuyActivity;
import cn.idealer01.infrastructure.dao.po.GroupBuyDiscount;
import cn.idealer01.infrastructure.dao.po.SCSkuActivity;
import cn.idealer01.infrastructure.dao.po.Sku;
import cn.idealer01.trigger.http.MarketIndexController;
import cn.idealer01.types.exception.AppException;
import org.junit.Test;
import org.springframework.jdbc.BadSqlGrammarException;

import java.math.BigDecimal;
import java.sql.SQLSyntaxErrorException;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MarketIndexControllerSkuListTest {

    @Test
    public void querySkuList_returnsSellableGoodsEvenWithoutMarketBinding() {
        ISkuDao skuDao = mock(ISkuDao.class);
        when(skuDao.querySkuList()).thenReturn(Arrays.asList(
                Sku.builder().source("s01").channel("c01").goodsId("9890001").goodsName("可售商品").originalPrice(new BigDecimal("89.90")).status(0).build(),
                Sku.builder().source("s01").channel("c01").goodsId("9890005").goodsName("未配置活动商品").originalPrice(new BigDecimal("19.90")).status(0).build()
        ));

        MarketIndexController controller = new MarketIndexController(null, skuDao);
        Response<SkuListResponseDTO> response = controller.querySkuList();

        assertEquals(2, response.getData().getSkuList().size());
        assertEquals("9890001", response.getData().getSkuList().get(0).getGoodsId());
        assertEquals("9890005", response.getData().getSkuList().get(1).getGoodsId());
    }

    @Test
    public void querySkuList_returnsGoodsWithoutImages_whenSkuImageTableMissing() {
        IIndexGroupBuyMarketService indexGroupBuyMarketService = mock(IIndexGroupBuyMarketService.class);
        ISkuDao skuDao = mock(ISkuDao.class);
        ISkuImageDao skuImageDao = mock(ISkuImageDao.class);
        when(skuDao.querySkuList()).thenReturn(Collections.singletonList(
                Sku.builder().source("s01").channel("c01").goodsId("9890001").goodsName("可售商品").originalPrice(new BigDecimal("89.90")).status(0).build()
        ));
        when(skuImageDao.querySkuImagesByGoodsIds(Collections.singletonList("9890001")))
                .thenThrow(new BadSqlGrammarException("query", "select * from sku_image", new SQLSyntaxErrorException("Table 'sku_image' doesn't exist")));

        MarketIndexController controller = new MarketIndexController(indexGroupBuyMarketService, skuDao, skuImageDao);
        Response<SkuListResponseDTO> response = controller.querySkuList();

        assertEquals("0000", response.getCode());
        assertEquals(1, response.getData().getSkuList().size());
        assertEquals("9890001", response.getData().getSkuList().get(0).getGoodsId());
        assertNull(response.getData().getSkuList().get(0).getCoverImageUrl());
    }

    @Test
    public void querySkuList_returnsMarketPriceAndRestrictedTagName() {
        ISkuDao skuDao = mock(ISkuDao.class);
        ISkuImageDao skuImageDao = mock(ISkuImageDao.class);
        ISCSkuActivityDao scSkuActivityDao = mock(ISCSkuActivityDao.class);
        IGroupBuyActivityDao activityDao = mock(IGroupBuyActivityDao.class);
        IGroupBuyDiscountDao discountDao = mock(IGroupBuyDiscountDao.class);
        ICrowdTagsDao tagsDao = mock(ICrowdTagsDao.class);
        when(skuDao.querySkuList()).thenReturn(Collections.singletonList(
                Sku.builder().source("s01").channel("c01").goodsId("9890001").goodsName("可售商品").originalPrice(new BigDecimal("99.00")).status(0).build()
        ));
        when(skuImageDao.querySkuImagesByGoodsIds(Collections.singletonList("9890001"))).thenReturn(Collections.emptyList());
        when(scSkuActivityDao.querySCSkuActivityBySCGoodsId(SCSkuActivity.builder().source("s01").channel("c01").goodsId("9890001").build()))
                .thenReturn(SCSkuActivity.builder().activityId(9890001L).goodsId("9890001").build());
        when(activityDao.queryGroupBuyActivityByActivityId(9890001L)).thenReturn(GroupBuyActivity.builder()
                .activityId(9890001L)
                .activityName("新人拼团")
                .discountId("1")
                .status(1)
                .tagId("T001")
                .tagScope("2")
                .build());
        when(discountDao.queryGroupBuyActivityDiscountByDiscountId("1")).thenReturn(GroupBuyDiscount.builder()
                .discountId(1)
                .marketPlan("ZJ")
                .marketExpr("10")
                .status(0)
                .build());
        when(tagsDao.queryCrowdTagsByTagId("T001")).thenReturn(CrowdTags.builder().tagId("T001").tagName("新人").build());

        MarketIndexController controller = new MarketIndexController(null, skuDao, skuImageDao, scSkuActivityDao, activityDao, discountDao, tagsDao);
        Response<SkuListResponseDTO> response = controller.querySkuList();

        SkuListResponseDTO.SkuItem item = response.getData().getSkuList().get(0);
        assertEquals(new BigDecimal("89.00"), item.getPayPrice());
        assertEquals(new BigDecimal("10.00"), item.getDeductionPrice());
        assertEquals(Long.valueOf(9890001L), item.getActivityId());
        assertEquals("新人拼团", item.getActivityName());
        assertEquals("新人", item.getTagName());
        assertEquals("2", item.getTagScope());
    }

    @Test
    public void queryGroupBuyMarketConfig_returnsPlainGoods_whenNoMarketConfig() throws Exception {
        IIndexGroupBuyMarketService indexGroupBuyMarketService = mock(IIndexGroupBuyMarketService.class);
        ISkuDao skuDao = mock(ISkuDao.class);
        when(indexGroupBuyMarketService.indexMarketTrial(any())).thenThrow(new AppException("E0002", "无拼团营销配置"));
        when(skuDao.querySkuByGoodsId("9890005")).thenReturn(Sku.builder()
                .source("s01")
                .channel("c01")
                .goodsId("9890005")
                .goodsName("未配置活动商品")
                .originalPrice(new BigDecimal("19.90"))
                .status(0)
                .build());

        MarketIndexController controller = new MarketIndexController(indexGroupBuyMarketService, skuDao);
        Response<GoodsMarketResponseDTO> response = controller.queryGroupBuyMarketConfig(
                GoodsMarketRequestDTO.builder()
                        .userId("u1")
                        .source("s01")
                        .channel("c01")
                        .goodsId("9890005")
                        .build()
        );

        assertEquals("0000", response.getCode());
        assertNull(response.getData().getActivityId());
        assertEquals("9890005", response.getData().getGoods().getGoodsId());
        assertEquals(new BigDecimal("19.90"), response.getData().getGoods().getOriginalPrice());
        assertEquals(new BigDecimal("0"), response.getData().getGoods().getDeductionPrice());
        assertEquals(new BigDecimal("19.90"), response.getData().getGoods().getPayPrice());
        assertEquals(Collections.emptyList(), response.getData().getTeamList());
        assertEquals(Integer.valueOf(0), response.getData().getTeamStatistic().getAllTeamCount());
        assertEquals(Boolean.TRUE, response.getData().getIsVisible());
        assertEquals(Boolean.TRUE, response.getData().getIsEnable());
    }

    @Test
    public void queryGroupBuyMarketConfig_returnsVisibilityAndEnableFlags() throws Exception {
        IIndexGroupBuyMarketService indexGroupBuyMarketService = mock(IIndexGroupBuyMarketService.class);
        ISkuDao skuDao = mock(ISkuDao.class);
        when(indexGroupBuyMarketService.indexMarketTrial(any())).thenReturn(TrialBalanceEntity.builder()
                .goodsId("9890001")
                .originalPrice(new BigDecimal("99.00"))
                .deductionPrice(new BigDecimal("10.00"))
                .payPrice(new BigDecimal("89.00"))
                .isVisible(true)
                .isEnable(false)
                .groupBuyActivityDiscountVO(GroupBuyActivityDiscountVO.builder()
                        .activityId(9890001L)
                        .activityName("新人拼团")
                        .groupType(1)
                        .target(3)
                        .validTime(15)
                        .tagId("T001")
                        .tagScope("2")
                        .build())
                .build());
        when(indexGroupBuyMarketService.queryTeamStatisticByActivity(9890001L)).thenReturn(TeamStatisticVO.builder()
                .allTeamCount(0)
                .allTeamCompleteCount(0)
                .allTeamUserCount(0)
                .build());

        MarketIndexController controller = new MarketIndexController(indexGroupBuyMarketService, skuDao);
        Response<GoodsMarketResponseDTO> response = controller.queryGroupBuyMarketConfig(
                GoodsMarketRequestDTO.builder()
                        .userId("u1")
                        .source("s01")
                        .channel("c01")
                        .goodsId("9890001")
                        .build()
        );

        assertEquals(Boolean.TRUE, response.getData().getIsVisible());
        assertEquals(Boolean.FALSE, response.getData().getIsEnable());
        assertEquals("新人拼团", response.getData().getActivity().getActivityName());
        assertEquals(Integer.valueOf(1), response.getData().getActivity().getGroupType());
        assertEquals(Integer.valueOf(3), response.getData().getActivity().getTarget());
        assertEquals(Integer.valueOf(15), response.getData().getActivity().getValidTime());
        assertEquals("T001", response.getData().getActivity().getTagId());
        assertEquals("2", response.getData().getActivity().getTagScope());
    }
}
