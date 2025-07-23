package io.f1.backend.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
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

        String username = "알수없는 사용자";
        if (accessor.getUser() != null) {
            username = accessor.getUser().getName();
        }

        if (command.equals(StompCommand.CONNECT)) {
            log.info("user : {} | CONNECT : 세션 연결 - sessionId = {}", username, sessionId);
        } else if (command.equals(StompCommand.SUBSCRIBE)) {
            if (destination != null && sessionId != null) {
                log.info("user : {} | SUBSCRIBE : 구독 시작 destination = {}", username, destination);
            }
        } else if (command.equals(StompCommand.SEND)) {
            log.info("user : {} | SEND : 요청 destination = {}", username, destination);
        } else if (command.equals(StompCommand.DISCONNECT)) {
            log.info("user : {} | DISCONNECT : 연결 해제 sessionId = {}", username, sessionId);
        }

        return message;
    }
}
