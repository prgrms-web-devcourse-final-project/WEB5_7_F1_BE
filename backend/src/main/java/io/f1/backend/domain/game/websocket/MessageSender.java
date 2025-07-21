package io.f1.backend.domain.game.websocket;

import io.f1.backend.domain.game.dto.MessageType;
import io.f1.backend.domain.game.dto.response.DefaultWebSocketResponse;
import io.f1.backend.domain.user.dto.UserPrincipal;
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

    public <T> void sendPersonal(String destination, MessageType type, T message, UserPrincipal principal) {
        System.out.println("MessageSend userPrincipal.getName() :"+principal.getName() );
        messagingTemplate.convertAndSendToUser(principal.getName(), destination, new DefaultWebSocketResponse<>(type, message));
    }
}
