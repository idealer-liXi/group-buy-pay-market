package cn.idealer01.test.trigger;

import cn.idealer01.infrastructure.dao.IOrderDao;
import cn.idealer01.infrastructure.dao.po.PayOrder;
import cn.idealer01.trigger.listener.TeamSuccessTopicListener;
import cn.idealer01.trigger.websocket.UserNotificationWebSocketHandler;
import org.junit.Test;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TeamSuccessTopicListenerTest {

    @Test
    public void pushesGroupSuccessToUsersResolvedFromOutTradeNos() {
        IOrderDao orderDao = mock(IOrderDao.class);
        UserNotificationWebSocketHandler webSocketHandler = mock(UserNotificationWebSocketHandler.class);
        when(orderDao.queryPayOrdersByOrderIds(Arrays.asList("o1", "o2"))).thenReturn(Arrays.asList(
                PayOrder.builder().userId("u1").orderId("o1").build(),
                PayOrder.builder().userId("u2").orderId("o2").build()
        ));

        TeamSuccessTopicListener listener = new TeamSuccessTopicListener(orderDao, webSocketHandler);
        listener.listener("{\"teamId\":\"t1\",\"outTradeNoList\":[\"o1\",\"o2\"]}");

        verify(webSocketHandler).sendToUsers(eq(Arrays.asList("u1", "u2")), contains("GROUP_SUCCESS"));
    }
}
