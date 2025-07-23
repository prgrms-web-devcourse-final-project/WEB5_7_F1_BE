package io.f1.backend.domain.game.websocket;

import io.f1.backend.domain.user.dto.UserPrincipal;

import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;

public class WebSocketUtils {

    public static String getSessionId(Message<?> message) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        return accessor.getSessionId();
    }

    public static UserPrincipal getSessionUser(Message<?> message) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        Authentication auth = (Authentication) accessor.getUser();
        return (UserPrincipal) auth.getPrincipal();
    }

    public static String getDestination(Long roomId) {
        return "/sub/room/" + roomId;
    }

    public static String getRoomSubscriptionDestination(Message<?> message) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
        return headerAccessor.getDestination();
    }
}
