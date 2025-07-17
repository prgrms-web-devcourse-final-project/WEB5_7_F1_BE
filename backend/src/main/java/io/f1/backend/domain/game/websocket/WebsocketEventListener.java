package io.f1.backend.domain.game.websocket;

import io.f1.backend.domain.game.app.RoomService;
import io.f1.backend.domain.user.dto.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebsocketEventListener {

    private final RoomService roomService;

    @EventListener
    public void handleWebsocketSubscribeListener(SessionSubscribeEvent event) {

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        log.info("sessionId = {}", sessionId);
        String destination = headerAccessor.getDestination();
        log.info("destination = {}", destination);

        String subStr = "/sub/room/";

        if(destination == null || !destination.startsWith(subStr)){
            return;
        }

        Long roomId = Long.parseLong(destination.substring(subStr.length()));

        Message<?> message = event.getMessage();
        UserPrincipal user = GameSocketController.getSessionUser(message);

        roomService.manageSession(roomId,sessionId, user.getUserId());
    }

}
