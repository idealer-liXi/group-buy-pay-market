package cn.idealer01.test.domain.order;

import cn.idealer01.domain.order.adapt.port.IProductPort;
import cn.idealer01.domain.order.adapt.reposity.IOrderRepository;
import cn.idealer01.domain.order.model.entity.OrderEntity;
import cn.idealer01.domain.order.model.valobj.MarketTypeVO;
import cn.idealer01.domain.order.model.valobj.OrderStatusVO;
import cn.idealer01.domain.order.service.OrderService;
import org.junit.Test;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrderServicePaymentSuccessTest {

    @Test
    public void changeOrderPaySuccess_ignoresClosedOrder() {
        IOrderRepository repository = mock(IOrderRepository.class);
        IProductPort productPort = mock(IProductPort.class);
        when(repository.queryOrderByOrderId("order-1")).thenReturn(OrderEntity.builder()
                .orderId("order-1")
                .orderStatusVO(OrderStatusVO.CLOSE)
                .marketType(MarketTypeVO.GROUP_BUY_MARKET.getCode())
                .build());

        TestOrderService orderService = new TestOrderService(repository, productPort);

        orderService.changeOrderPaySuccess("order-1", new Date());

        verify(repository, never()).changeMarketOrderPaySuccess("order-1");
        verify(repository, never()).changeOrderPaySuccess(eq("order-1"), any(Date.class));
    }

    private static class TestOrderService extends OrderService {
        TestOrderService(IOrderRepository repository, IProductPort port) {
            super(repository, port);
        }
    }
}
