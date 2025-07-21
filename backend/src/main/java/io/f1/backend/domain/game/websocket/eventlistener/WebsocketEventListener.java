package io.f1.backend.domain.game.websocket.eventlistener;

import static io.f1.backend.domain.game.websocket.WebSocketUtils.getSessionId;
import static io.f1.backend.domain.game.websocket.WebSocketUtils.getSessionUser;

import io.f1.backend.domain.game.websocket.Service.SessionService;
import io.f1.backend.domain.user.dto.UserPrincipal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebsocketEventListener {

    private final SessionService sessionService;

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
        Message<?> message = event.getMessage();

        String sessionId = getSessionId(message);
        UserPrincipal user = getSessionUser(message);

        sessionService.addSession(sessionId, user.getUserId());
    }

    @EventListener
    public void handleWebsocketSubscribeListener(SessionSubscribeEvent event) {

        Message<?> message = event.getMessage();

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);

        String sessionId = getSessionId(message);
        UserPrincipal user = getSessionUser(message);
        Long userId = user.getUserId();
        String destination = headerAccessor.getDestination();

        if (destination == null) {
            // todo 에러처리: 잘못된 구독 주소입니다
            return;
        }

        String[] subscribeType = destination.split("/");

        if (subscribeType[2].equals("room")) {
            Long roomId = Long.parseLong(subscribeType[3]);
            sessionService.addRoomId(roomId, sessionId);
            sessionService.handleUserReconnect(roomId, sessionId, userId);
        }

        sessionService.removeSession(sessionId, userId);
    }

    @EventListener
    public void handleWebsocketDisconnectedListener(SessionDisconnectEvent event) {

        Message<?> message = event.getMessage();

        String sessionId = getSessionId(message);
        UserPrincipal principal = getSessionUser(message);

        sessionService.handleUserDisconnect(sessionId, principal);
    }
}
