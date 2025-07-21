package io.f1.backend.domain.game.websocket;

import io.f1.backend.domain.user.dto.UserPrincipal;

import io.f1.backend.domain.user.entity.User;
import java.time.LocalDateTime;
import java.util.HashMap;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;

public class WebSocketUtils {

    public static String getSessionId(Message<?> message) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        return "sessionId";
        //return accessor.getSessionId();
    }

    public static UserPrincipal getSessionUser(Message<?> message) {
        //        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
//        Authentication auth = (Authentication) accessor.getUser();
//        return (UserPrincipal) auth.getPrincipal();
        User user = new User("provider","providerId", LocalDateTime.now());
        user.setId(1L);
        return new UserPrincipal(user,new HashMap<String,Object>());
    }
}
