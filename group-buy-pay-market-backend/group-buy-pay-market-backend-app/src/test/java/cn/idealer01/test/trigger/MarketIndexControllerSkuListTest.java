package cn.idealer01.test.trigger;

import cn.idealer01.api.dto.GoodsMarketRequestDTO;
import cn.idealer01.api.dto.GoodsMarketResponseDTO;
import cn.idealer01.api.dto.SkuListResponseDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.domain.activity.service.IIndexGroupBuyMarketService;
import cn.idealer01.infrastructure.dao.ISkuDao;
import cn.idealer01.infrastructure.dao.po.Sku;
import cn.idealer01.trigger.http.MarketIndexController;
import cn.idealer01.types.exception.AppException;
import org.junit.Test;

import java.math.BigDecimal;
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
    }
}
