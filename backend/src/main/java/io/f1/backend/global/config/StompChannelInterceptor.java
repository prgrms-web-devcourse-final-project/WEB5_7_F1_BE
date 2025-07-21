package io.f1.backend.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();
        String sessionId = accessor.getSessionId();
        String destination = accessor.getDestination();

        if (command == null) {
            throw new IllegalArgumentException("Stomp command required");
        }

        if (command.equals(StompCommand.CONNECT)) {
            Authentication auth = (Authentication) accessor.getSessionAttributes().get("auth");
            log.info("WebSocket CONNECT Principal name: {}", auth.getName());
            log.info("CONNECT : 세션 연결 - sessionId = {}", sessionId);
        } else if (command.equals(StompCommand.SUBSCRIBE)) {
            if (destination != null && sessionId != null) {
                log.info("SUBSCRIBE : 구독 시작 destination = {}", destination);
            }
        } else if (command.equals(StompCommand.SEND)) {
            log.info("SEND : 요청 destination = {}", destination);
        } else if (command.equals(StompCommand.DISCONNECT)) {
            log.info("DISCONNECT : 연결 해제 sessionId = {}", sessionId);
        }

        return message;
    }
}
