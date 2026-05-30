package cn.idealer01.test.domain.order;

import cn.idealer01.domain.order.service.OrderService;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class OrderServiceAlipayAmountTest {

    @Test
    public void formatAlipayTotalAmount_usesExactlyTwoDecimalPlaces() {
        assertEquals("32.70", OrderService.formatAlipayTotalAmount(new BigDecimal("32.700")));
        assertEquals("9.90", OrderService.formatAlipayTotalAmount(new BigDecimal("9.9")));
    }
}
