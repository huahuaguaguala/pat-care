package com.petcare.config;
import com.petcare.websocket.OrderWebSocketHandler;
import com.petcare.websocket.WebSocketHandshakeInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Autowired private OrderWebSocketHandler handler;
    @Autowired private WebSocketHandshakeInterceptor interceptor;

    @Override
    public void registerWebSocketHa