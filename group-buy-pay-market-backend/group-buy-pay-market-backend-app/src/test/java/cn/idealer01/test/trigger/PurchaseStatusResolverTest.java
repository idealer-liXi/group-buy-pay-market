package cn.idealer01.test.trigger;

import cn.idealer01.infrastructure.dao.po.PayOrder;
import cn.idealer01.trigger.http.PurchaseStatusResolver;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class PurchaseStatusResolverTest {

    private final PurchaseStatusResolver resolver = new PurchaseStatusResolver();

    @Test
    public void resolve_returnsWaitPayForPlainPayWaitWithPayUrl() {
        PayOrder order = PayOrder.builder().status("PAY_WAIT").marketType(0).payUrl("pay-form").build();

        assertEquals("WAIT_PAY", resolver.resolve(order));
    }

    @Test
    public void resolve_returnsWaitPayForGroupCreateLock() {
        PayOrder order = PayOrder.builder().status("CREATE").marketType(1).build();

        assertEquals("WAIT_PAY", resolver.resolve(order));
    }

    @Test
    public void resolve_returnsGroupWaitForPaidValidGroup() {
        PayOrder order = PayOrder.builder()
                .status("PAY_SUCCESS")
                .marketType(1)
                .groupOrderListStatus(1)
                .groupOrderStatus(0)
                .groupValidEndTime(new Date(System.currentTimeMillis() + 60000))
                .build();

        assertEquals("GROUP_WAIT", resolver.resolve(order));
    }

    @Test
    public void resolve_returnsClosedForRefundedGroupDetail() {
        PayOrder order = PayOrder.builder()
                .status("PAY_SUCCESS")
                .marketType(1)
                .groupOrderListStatus(2)
                .groupOrderStatus(0)
                .build();

        assertEquals("CLOSED", resolver.resolve(order));
    }

    @Test
    public void resolve_returnsClosedForExpiredPaidGroup() {
        PayOrder order = PayOrder.builder()
                .status("PAY_SUCCESS")
                .marketType(1)
                .groupOrderListStatus(1)
                .groupOrderStatus(0)
                .groupValidEndTime(new Date(System.currentTimeMillis() - 60000))
                .build();

        assertEquals("CLOSED", resolver.resolve(order));
    }

    @Test
    public void resolve_returnsSuccessForCompleteGroup() {
        PayOrder order = PayOrder.builder()
                .status("PAY_SUCCESS")
                .marketType(1)
                .groupOrderListStatus(1)
                .groupOrderStatus(1)
                .build();

        assertEquals("GROUP_SUCCESS", resolver.resolve(order));
    }

    @Test
    public void resolve_returnsSuccessForPlainPaidOrder() {
        PayOrder order = PayOrder.builder().status("PAY_SUCCESS").marketType(0).build();

        assertEquals("GROUP_SUCCESS", resolver.resolve(order));
    }

    @Test
    public void resolve_returnsClosedForClosedPayOrder() {
        PayOrder order = PayOrder.builder().status("CLOSE").marketType(0).build();

        assertEquals("CLOSED", resolver.resolve(order));
    }
}
