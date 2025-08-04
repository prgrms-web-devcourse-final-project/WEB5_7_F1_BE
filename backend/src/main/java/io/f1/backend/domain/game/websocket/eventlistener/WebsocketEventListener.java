package io.f1.backend.domain.game.websocket.eventlistener;

import static io.f1.backend.domain.game.app.RoomService.ROOM_LOCK_PREFIX;
import static io.f1.backend.domain.game.app.RoomService.USER_LOCK_PREFIX;
import static io.f1.backend.domain.game.websocket.WebSocketUtils.getSessionUser;

import io.f1.backend.domain.game.app.RoomService;
import io.f1.backend.domain.game.model.ConnectionState;
import io.f1.backend.domain.game.websocket.DisconnectTaskManager;
import io.f1.backend.domain.game.websocket.HeartbeatMonitor;
import io.f1.backend.domain.user.dto.UserPrincipal;
import io.f1.backend.global.lock.LockExecutor;

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
    private final LockExecutor lockExecutor;

    @EventListener
    public void handleDisconnectedListener(SessionDisconnectEvent event) {

        Message<?> message = event.getMessage();
        UserPrincipal principal = getSessionUser(message);

        Long userId = principal.getUserId();
        String sessionId = event.getSessionId();

        heartbeatMonitor.cleanSession(sessionId);
        Long roomId = roomService.getRoomIdBySessionId(sessionId);
        roomService.removeSessionRoomId(sessionId);

        /* 정상 로직 */
        if (!roomService.isUserInAnyRoom(userId)) {
            return;
        }

        if (!roomService.existsRoom(roomId)) {
            return;
        }

        lockExecutor.executeWithLock(
                ROOM_LOCK_PREFIX,
                roomId,
                () ->
                        roomService.changeConnectedStatus(
                                roomId, userId, ConnectionState.DISCONNECTED));

        taskManager.scheduleDisconnectTask(
                userId,
                () -> {
                    lockExecutor.executeWithLock(
                            USER_LOCK_PREFIX,
                            userId,
                            () -> {
                                lockExecutor.executeWithLock(
                                        ROOM_LOCK_PREFIX,
                                        roomId,
                                        () -> {
                                            if (ConnectionState.DISCONNECTED.equals(
                                                    roomService.getPlayerState(userId, roomId))) {
                                                roomService.disconnectOrExitRoom(roomId, principal);
                                            }
                                        });
                            });
                });
    }
}
