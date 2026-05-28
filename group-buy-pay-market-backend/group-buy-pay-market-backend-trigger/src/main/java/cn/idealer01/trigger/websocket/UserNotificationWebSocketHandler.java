package cn.idealer01.trigger.websocket;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class UserNotificationWebSocketHandler extends TextWebSocketHandler {

    private final ConcurrentHashMap<String, Set<WebSocketSession>> userSessionMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> sessionUserMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = resolveUserId(session);
        if (StringUtils.isBlank(userId)) {
            return;
        }
        userSessionMap.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet()).add(session);
        sessionUserMap.put(session.getId(), userId);
        log.info("用户通知 WebSocket 已连接 userId:{} sessionId:{}", userId, session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        removeSession(session);
    }

    public void sendToUsers(Collection<String> userIds, String message) {
        for (String userId : userIds) {
            Set<WebSocketSession> sessions = userSessionMap.get(userId);
            if (sessions == null || sessions.isEmpty()) {
                continue;
            }
            for (WebSocketSession session : sessions) {
                send(session, message);
            }
        }
    }

    private void send(WebSocketSession session, String message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            } else {
                removeSession(session);
            }
        } catch (IOException e) {
            log.warn("用户通知 WebSocket 发送失败 sessionId:{}", session.getId(), e);
            removeSession(session);
        }
    }

    private String resolveUserId(WebSocketSession session) {
        Object userId = session.getAttributes().get("userId");
        if (userId == null) {
            return null;
        }
        return String.valueOf(userId);
    }

    private void removeSession(WebSocketSession session) {
        String userId = sessionUserMap.remove(session.getId());
        if (StringUtils.isBlank(userId)) {
            return;
        }
        Set<WebSocketSession> sessions = userSessionMap.get(userId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                userSessionMap.remove(userId);
            }
        }
    }
}
