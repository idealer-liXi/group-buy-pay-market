package cn.idealer01.test.trigger;

import cn.idealer01.api.dto.GoodsMarketRequestDTO;
import cn.idealer01.api.dto.GoodsMarketResponseDTO;
import cn.idealer01.api.dto.SkuListResponseDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.domain.activity.model.entity.TrialBalanceEntity;
import cn.idealer01.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.idealer01.domain.activity.model.valobj.TeamStatisticVO;
import cn.idealer01.domain.activity.service.IIndexGroupBuyMarketService;
import cn.idealer01.infrastructure.dao.ISkuDao;
import cn.idealer01.infrastructure.dao.ISkuImageDao;
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
                        .target(3)
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
    }
}
