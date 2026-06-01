package com.petcare.websocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OrderWebSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(OrderWebSocketHandler.class);
    private static final ConcurrentHashMap<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) sessions.put(userId, session);
        log.info("WebSocket connected: userId={}", userId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) sessions.remove(userId);
        log.info("WebSocket closed: userId={}", userId);
    }

    public void sendToUser(Long userId, String message) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try { session.sendMessage(new TextMessage(message)); } catch (Exception e) { log.error("send error", e); }
        }
    }
}
