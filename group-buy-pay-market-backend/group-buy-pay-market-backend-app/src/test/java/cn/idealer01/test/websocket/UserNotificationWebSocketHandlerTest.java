package cn.idealer01.test.websocket;

import cn.idealer01.trigger.websocket.UserNotificationWebSocketHandler;
import org.junit.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserNotificationWebSocketHandlerTest {

    @Test
    public void sendsMessageToAllOnlineSessionsForUser() throws Exception {
        UserNotificationWebSocketHandler handler = new UserNotificationWebSocketHandler();
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn("session-1");
        when(session.getUri()).thenReturn(new URI("ws://localhost:8080/ws/notifications"));
        when(session.getAttributes()).thenReturn(new HashMap<String, Object>() {{ put("userId", "u1"); }});
        when(session.isOpen()).thenReturn(true);

        handler.afterConnectionEstablished(session);
        handler.sendToUsers(Collections.singleton("u1"), "{\"type\":\"GROUP_SUCCESS\"}");

        verify(session).sendMessage(any(TextMessage.class));
    }
}
