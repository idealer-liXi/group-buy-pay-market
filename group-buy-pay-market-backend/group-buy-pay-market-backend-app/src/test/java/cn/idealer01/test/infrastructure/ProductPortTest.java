package cn.idealer01.test.infrastructure;

import cn.idealer01.domain.order.model.entity.ProductEntity;
import cn.idealer01.infrastructure.adapter.port.LocalGroupBuyMarketPort;
import cn.idealer01.infrastructure.adapter.port.ProductPort;
import cn.idealer01.infrastructure.dao.ISkuDao;
import cn.idealer01.infrastructure.dao.po.Sku;
import cn.idealer01.infrastructure.gateway.ProductRPC;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class ProductPortTest {

    @Test
    public void queryProductByProductId_returnsSkuOriginalPrice_forPlainProductPayment() {
        ProductRPC productRPC = mock(ProductRPC.class);
        LocalGroupBuyMarketPort localGroupBuyMarketPort = mock(LocalGroupBuyMarketPort.class);
        ISkuDao skuDao = mock(ISkuDao.class);
        when(skuDao.querySkuByGoodsId("9890005")).thenReturn(Sku.builder()
                .goodsId("9890005")
                .goodsName("新商品")
                .originalPrice(new BigDecimal("19.90"))
                .status(0)
                .build());

        ProductPort productPort = new ProductPort(productRPC, localGroupBuyMarketPort, skuDao);
        ProductEntity productEntity = productPort.queryProductByProductId("9890005");

        assertEquals("9890005", productEntity.getProductId());
        assertEquals("新商品", productEntity.getProductName());
        assertEquals(new BigDecimal("19.90"), productEntity.getPrice());
        verifyNoInteractions(productRPC);
    }
}
