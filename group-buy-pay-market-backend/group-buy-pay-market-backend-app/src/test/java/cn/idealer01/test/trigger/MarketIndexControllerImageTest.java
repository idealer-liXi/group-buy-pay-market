package cn.idealer01.test.trigger;

import cn.idealer01.api.dto.GoodsMarketRequestDTO;
import cn.idealer01.api.dto.GoodsMarketResponseDTO;
import cn.idealer01.api.dto.SkuListResponseDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.domain.activity.service.IIndexGroupBuyMarketService;
import cn.idealer01.infrastructure.dao.ISkuDao;
import cn.idealer01.infrastructure.dao.ISkuImageDao;
import cn.idealer01.infrastructure.dao.po.Sku;
import cn.idealer01.infrastructure.dao.po.SkuImage;
import cn.idealer01.trigger.http.MarketIndexController;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MarketIndexControllerImageTest {

    @Test
    public void querySkuList_returnsCoverImageUrl() {
        ISkuDao skuDao = mock(ISkuDao.class);
        ISkuImageDao skuImageDao = mock(ISkuImageDao.class);
        when(skuDao.querySkuList()).thenReturn(Arrays.asList(Sku.builder().goodsId("9890001").goodsName("商品").originalPrice(new BigDecimal("9.90")).status(0).build()));
        when(skuImageDao.querySkuImagesByGoodsIds(Arrays.asList("9890001"))).thenReturn(Arrays.asList(SkuImage.builder().goodsId("9890001").imageUrl("https://cdn.example.com/1.png").sortOrder(1).build()));

        MarketIndexController controller = new MarketIndexController(null, skuDao, skuImageDao);
        Response<SkuListResponseDTO> response = controller.querySkuList();

        assertEquals("https://cdn.example.com/1.png", response.getData().getSkuList().get(0).getCoverImageUrl());
    }

    @Test
    public void queryPlainGoodsMarketConfig_returnsGoodsNameAndImages() throws Exception {
        IIndexGroupBuyMarketService marketService = mock(IIndexGroupBuyMarketService.class);
        ISkuDao skuDao = mock(ISkuDao.class);
        ISkuImageDao skuImageDao = mock(ISkuImageDao.class);
        when(marketService.indexMarketTrial(any())).thenThrow(new AppException(ResponseCode.E0002.getCode(), ResponseCode.E0002.getInfo()));
        when(skuDao.querySkuByGoodsId("9890005")).thenReturn(Sku.builder().goodsId("9890005").goodsName("普通商品").originalPrice(new BigDecimal("19.90")).status(0).build());
        when(skuImageDao.querySkuImagesByGoodsId("9890005")).thenReturn(Arrays.asList(SkuImage.builder().goodsId("9890005").imageUrl("https://cdn.example.com/p.png").sortOrder(1).build()));

        MarketIndexController controller = new MarketIndexController(marketService, skuDao, skuImageDao);
        Response<GoodsMarketResponseDTO> response = controller.queryGroupBuyMarketConfig(GoodsMarketRequestDTO.builder().userId("u1").source("s01").channel("c01").goodsId("9890005").build());

        assertEquals("普通商品", response.getData().getGoods().getGoodsName());
        assertEquals("https://cdn.example.com/p.png", response.getData().getGoods().getCoverImageUrl());
        assertEquals(1, response.getData().getGoods().getImageUrls().size());
    }
}
