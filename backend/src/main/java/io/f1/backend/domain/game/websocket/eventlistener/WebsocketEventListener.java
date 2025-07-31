package io.f1.backend.domain.game.websocket.eventlistener;

import static io.f1.backend.domain.game.websocket.WebSocketUtils.getSessionUser;

import io.f1.backend.domain.game.app.RoomService;
import io.f1.backend.domain.game.model.ConnectionState;
import io.f1.backend.domain.game.websocket.DisconnectTaskManager;
import io.f1.backend.domain.game.websocket.HeartbeatMonitor;
import io.f1.backend.domain.user.dto.UserPrincipal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebsocketEventListener {

    private final RoomService roomService;
    private final DisconnectTaskManager taskManager;
    private final HeartbeatMonitor heartbeatMonitor;

    @EventListener
    public void handleDisconnectedListener(SessionDisconnectEvent event) {

        Message<?> message = event.getMessage();
        UserPrincipal principal = getSessionUser(message);

        Long userId = principal.getUserId();

        //todo FE 개발 될때까지 주석 처리
        //heartbeatMonitor.cleanSession(event.getSessionId());

        /* 정상 로직 */
        if (!roomService.isUserInAnyRoom(userId)) {
            return;
        }

        Long roomId = roomService.getRoomIdByUserId(userId);

        roomService.changeConnectedStatus(roomId, userId, ConnectionState.DISCONNECTED);

        taskManager.scheduleDisconnectTask(
                userId,
                () -> {
                    if (ConnectionState.DISCONNECTED.equals(
                            roomService.getPlayerState(userId, roomId))) {
                        roomService.disconnectOrExitRoom(roomId, principal);
                    }
                });
    }
}
