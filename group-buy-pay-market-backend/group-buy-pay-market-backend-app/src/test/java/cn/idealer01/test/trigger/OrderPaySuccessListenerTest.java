package cn.idealer01.test.trigger;

import cn.idealer01.domain.goods.service.IGoodsService;
import cn.idealer01.infrastructure.dao.IOrderDao;
import cn.idealer01.infrastructure.dao.po.PayOrder;
import cn.idealer01.trigger.listener.OrderPaySuccessListener;
import cn.idealer01.trigger.websocket.UserNotificationWebSocketHandler;
import org.junit.Test;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrderPaySuccessListenerTest {

    @Test
    public void pushesPaySuccessToOrderUser() {
        IGoodsService goodsService = mock(IGoodsService.class);
        IOrderDao orderDao = mock(IOrderDao.class);
        UserNotificationWebSocketHandler webSocketHandler = mock(UserNotificationWebSocketHandler.class);
        when(orderDao.queryPayOrderByOrderId("o1")).thenReturn(PayOrder.builder().userId("u1").orderId("o1").build());

        OrderPaySuccessListener listener = new OrderPaySuccessListener(goodsService, orderDao, webSocketHandler);
        listener.listener("{\"tradeNo\":\"o1\"}");

        verify(goodsService).changeOrderDealDone("o1");
        verify(webSocketHandler).sendToUsers(eq(Collections.singleton("u1")), contains("PAY_SUCCESS"));
    }
}
