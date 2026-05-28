package cn.idealer01.trigger.websocket;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class UserNotificationHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String userId = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams().getFirst("userId");
        String loginToken = getCookieValue(request.getHeaders().get(HttpHeaders.COOKIE), "loginToken");
        if (StringUtils.isBlank(userId) || !userId.equals(loginToken)) {
            return false;
        }

        attributes.put("userId", userId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
    }

    private String getCookieValue(List<String> cookieHeaders, String name) {
        if (cookieHeaders == null || cookieHeaders.isEmpty()) {
            return null;
        }

        for (String cookieHeader : cookieHeaders) {
            String[] cookies = cookieHeader.split(";");
            for (String cookie : cookies) {
                String[] pair = cookie.trim().split("=", 2);
                if (pair.length == 2 && name.equals(pair[0])) {
                    try {
                        return URLDecoder.decode(pair[1], StandardCharsets.UTF_8.name());
                    } catch (UnsupportedEncodingException e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }
}
