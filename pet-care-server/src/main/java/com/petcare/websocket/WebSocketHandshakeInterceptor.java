package com.petcare.websocket;

import com.petcare.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 握手拦截器 — 从 query string 中提取 JWT token 并解析 userId
 * 小程序连接示例: wx.connectSocket({ url: 'ws://localhost:8080/ws/order?token=xxx' })
 */
@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String query = request.getURI().getQuery();
        if (query == null || !query.contains("token=")) {
            return true; // 允许连接，但不会推送（userId=null）
        }

        String token = null;
        for (String param : query.split("&")) {
            if (param.startsWith("token=")) {
                token = param.substring(6);
                break;
            }
        }

        if (token != null && jwtUtils.validateToken(token)) {
            attributes.put("userId", jwtUtils.getUserId(token));
            attributes.put("role", jwtUtils.getRole(token));
        }

        return true; // 始终允许握手
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }
}
