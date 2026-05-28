package cn.idealer01.test.websocket;

import cn.idealer01.trigger.websocket.UserNotificationHandshakeInterceptor;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserNotificationHandshakeInterceptorTest {

    @Test
    public void acceptsOnlyWhenCookieLoginTokenMatchesUserIdQuery() throws Exception {
        UserNotificationHandshakeInterceptor interceptor = new UserNotificationHandshakeInterceptor();
        ServerHttpRequest request = mockRequest("ws://localhost:8080/ws/notifications?userId=u1", "loginToken=u1");
        Map<String, Object> attributes = new HashMap<>();

        assertTrue(interceptor.beforeHandshake(request, null, null, attributes));
        assertTrue(attributes.containsKey("userId"));
    }

    @Test
    public void rejectsMismatchedCookieLoginTokenAndUserIdQuery() throws Exception {
        UserNotificationHandshakeInterceptor interceptor = new UserNotificationHandshakeInterceptor();
        ServerHttpRequest request = mockRequest("ws://localhost:8080/ws/notifications?userId=victim", "loginToken=attacker");

        assertFalse(interceptor.beforeHandshake(request, null, null, new HashMap<>()));
    }

    private ServerHttpRequest mockRequest(String uri, String cookie) {
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, cookie);
        when(request.getURI()).thenReturn(URI.create(uri));
        when(request.getHeaders()).thenReturn(headers);
        return request;
    }
}
