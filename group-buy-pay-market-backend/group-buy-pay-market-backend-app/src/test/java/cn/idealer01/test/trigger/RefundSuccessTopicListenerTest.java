package cn.idealer01.test.trigger;

import cn.idealer01.domain.trade.service.ITradeRefundOrderService;
import cn.idealer01.trigger.listener.RefundSuccessTopicListener;
import cn.idealer01.trigger.websocket.UserNotificationWebSocketHandler;
import org.junit.Test;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class RefundSuccessTopicListenerTest {

    @Test
    public void pushesGroupFailForUnformedRefund() {
        ITradeRefundOrderService refundOrderService = mock(ITradeRefundOrderService.class);
        UserNotificationWebSocketHandler webSocketHandler = mock(UserNotificationWebSocketHandler.class);

        RefundSuccessTopicListener listener = new RefundSuccessTopicListener(refundOrderService, webSocketHandler);
        listener.listener("{\"type\":\"paid_unformed\",\"userId\":\"u1\",\"teamId\":\"t1\",\"orderId\":\"o1\",\"outTradeNo\":\"o1\"}");

        verify(webSocketHandler).sendToUsers(eq(Collections.singleton("u1")), contains("GROUP_FAIL"));
    }

    @Test
    public void pushesGroupFailForEnumNameUnformedRefund() {
        ITradeRefundOrderService refundOrderService = mock(ITradeRefundOrderService.class);
        UserNotificationWebSocketHandler webSocketHandler = mock(UserNotificationWebSocketHandler.class);

        RefundSuccessTopicListener listener = new RefundSuccessTopicListener(refundOrderService, webSocketHandler);
        listener.listener("{\"type\":\"PAID_UNFORMED\",\"userId\":\"u1\",\"teamId\":\"t1\",\"orderId\":\"o1\",\"outTradeNo\":\"o1\"}");

        verify(webSocketHandler).sendToUsers(eq(Collections.singleton("u1")), contains("GROUP_FAIL"));
    }

    @Test
    public void pushesRefundSuccessForFormedRefund() {
        ITradeRefundOrderService refundOrderService = mock(ITradeRefundOrderService.class);
        UserNotificationWebSocketHandler webSocketHandler = mock(UserNotificationWebSocketHandler.class);

        RefundSuccessTopicListener listener = new RefundSuccessTopicListener(refundOrderService, webSocketHandler);
        listener.listener("{\"type\":\"paid_formed\",\"userId\":\"u1\",\"teamId\":\"t1\",\"orderId\":\"o1\",\"outTradeNo\":\"o1\"}");

        verify(webSocketHandler).sendToUsers(eq(Collections.singleton("u1")), contains("REFUND_SUCCESS"));
    }
}
