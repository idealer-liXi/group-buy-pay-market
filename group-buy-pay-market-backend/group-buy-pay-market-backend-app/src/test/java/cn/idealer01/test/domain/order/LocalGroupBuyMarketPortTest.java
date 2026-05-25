package cn.idealer01.test.domain.order;

import cn.idealer01.api.dto.LockMarketPayOrderResponseDTO;
import cn.idealer01.domain.order.model.valobj.MarketPayDiscountEntity;
import cn.idealer01.infrastructure.adapter.port.LocalGroupBuyMarketPort;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

public class LocalGroupBuyMarketPortTest {

    @Test
    public void shouldTranslateTradeLockResultIntoOrderDiscountEntity() {
        LockMarketPayOrderResponseDTO responseDTO = LockMarketPayOrderResponseDTO.builder()
                .originalPrice(new BigDecimal("100.00"))
                .deductionPrice(new BigDecimal("10.00"))
                .payPrice(new BigDecimal("90.00"))
                .build();

        MarketPayDiscountEntity entity = LocalGroupBuyMarketPort.toMarketPayDiscountEntity(responseDTO);

        Assert.assertEquals(new BigDecimal("100.00"), entity.getOriginalPrice());
        Assert.assertEquals(new BigDecimal("10.00"), entity.getDeductionPrice());
        Assert.assertEquals(new BigDecimal("90.00"), entity.getPayPrice());
    }
}
