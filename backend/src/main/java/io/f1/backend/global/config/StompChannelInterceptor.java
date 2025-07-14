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

        switch (command) {
            case CONNECT -> log.info("CONNECT : 세션 연결 - sessionId = {}", sessionId);

            case SUBSCRIBE -> {
                if (destination != null && sessionId != null) {
                    log.info("SUBSCRIBE : 구독 시작 destination = {}", destination);
                }
            }

            case SEND -> log.info("SEND : 요청 destination = {}", destination);

            case DISCONNECT -> log.info("DISCONNECT : 연결 해제 sessionId = {}", sessionId);

            default -> throw new IllegalStateException("Unexpected command: " + command);
        }

        return message;
    }
}
