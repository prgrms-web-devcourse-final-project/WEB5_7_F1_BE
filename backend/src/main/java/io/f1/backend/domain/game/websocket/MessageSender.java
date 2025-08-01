package io.f1.backend.domain.game.websocket;

import io.f1.backend.domain.game.dto.MessageType;
import io.f1.backend.domain.game.dto.response.DefaultWebSocketResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageSender {

    private final SimpMessagingTemplate messagingTemplate;

    public <T> void sendBroadcast(String destination, MessageType type, T message) {
        messagingTemplate.convertAndSend(
                destination, new DefaultWebSocketResponse<>(type, message));
    }

    public <T> void sendPersonal(
            String destination, MessageType type, T message, String principalName) {
        messagingTemplate.convertAndSendToUser(
                principalName, destination, new DefaultWebSocketResponse<>(type, message));
    }
}
