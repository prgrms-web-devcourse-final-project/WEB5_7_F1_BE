package io.f1.backend.domain.game.websocket.eventlistener;

import io.f1.backend.domain.game.app.RoomService;
import io.f1.backend.domain.game.websocket.controller.GameSocketController;
import io.f1.backend.domain.user.dto.UserPrincipal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
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

        Message<?> message = event.getMessage();
        UserPrincipal user = GameSocketController.getSessionUser(message);

        log.info("sessionId = {}", sessionId);
        String destination = headerAccessor.getDestination();
        log.info("destination = {}", destination);

        if(destination == null || !destination.startsWith("/sub/")){
            //todo 에러처리: 잘못된 구독 주소입니다
            return;
        }

        String[] subscribeType = destination.split("/");

        if(subscribeType[2].equals("room")){
            Long roomId = Long.parseLong(subscribeType[3]);

            roomService.manageConnectSession(roomId,sessionId, user.getUserId());
        }

    }

    @EventListener
    public void handleWebsocketDisconnectedListener(SessionDisconnectEvent event) {

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        Message<?> message = event.getMessage();
        UserPrincipal user = GameSocketController.getSessionUser(message);

        String subStr = "/sub/room/";

        String destination = headerAccessor.getDestination();
        Long roomId = Long.parseLong(destination.substring(subStr.length()));

        //disconnected 처리

    }

}
