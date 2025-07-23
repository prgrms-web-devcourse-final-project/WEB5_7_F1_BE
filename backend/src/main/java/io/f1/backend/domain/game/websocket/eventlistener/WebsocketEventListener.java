package io.f1.backend.domain.game.websocket.eventlistener;

import static io.f1.backend.domain.game.websocket.WebSocketUtils.getRoomSubscriptionDestination;
import static io.f1.backend.domain.game.websocket.WebSocketUtils.getSessionId;
import static io.f1.backend.domain.game.websocket.WebSocketUtils.getSessionUser;

import io.f1.backend.domain.game.websocket.service.SessionService;
import io.f1.backend.domain.user.dto.UserPrincipal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
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
    public void handleConnectListener(SessionConnectEvent event) {
        Message<?> message = event.getMessage();

        String sessionId = getSessionId(message);
        UserPrincipal user = getSessionUser(message);

        sessionService.addSession(sessionId, user.getUserId());
    }

    @EventListener
    public void handleSubscribeListener(SessionSubscribeEvent event) {

        Message<?> message = event.getMessage();

        String sessionId = getSessionId(message);

        String destination = getRoomSubscriptionDestination(message);

        // todo 인덱스 길이 유효성 추가
        String[] subscribeType = destination.split("/");

        if (subscribeType[2].equals("room")) {
            Long roomId = Long.parseLong(subscribeType[3]);
            sessionService.addRoomId(roomId, sessionId);
        }
    }

    @EventListener
    public void handleDisconnectedListener(SessionDisconnectEvent event) {

        Message<?> message = event.getMessage();

        String sessionId = getSessionId(message);
        UserPrincipal principal = getSessionUser(message);

        sessionService.handleUserDisconnect(sessionId, principal);
    }
}
