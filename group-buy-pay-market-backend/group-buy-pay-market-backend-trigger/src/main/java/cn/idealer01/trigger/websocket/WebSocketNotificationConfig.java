package cn.idealer01.trigger.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.annotation.Resource;

@Configuration
@EnableWebSocket
public class WebSocketNotificationConfig implements WebSocketConfigurer {

    @Resource
    private UserNotificationWebSocketHandler userNotificationWebSocketHandler;
    @Resource
    private UserNotificationHandshakeInterceptor userNotificationHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(userNotificationWebSocketHandler, "/ws/notifications")
                .addInterceptors(userNotificationHandshakeInterceptor)
                .setAllowedOrigins("http://127.0.0.1:5173", "http://localhost:5173", "http://127.0.0.1:8080", "http://localhost:8080");
    }
}
