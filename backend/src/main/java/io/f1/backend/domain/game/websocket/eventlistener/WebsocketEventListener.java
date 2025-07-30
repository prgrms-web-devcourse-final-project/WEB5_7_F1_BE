package io.f1.backend.domain.game.websocket.eventlistener;

import static io.f1.backend.domain.game.websocket.WebSocketUtils.getSessionUser;

import io.f1.backend.domain.game.app.RoomService;
import io.f1.backend.domain.game.model.ConnectionState;
import io.f1.backend.domain.game.websocket.DisconnectTaskManager;
import io.f1.backend.domain.user.dto.UserPrincipal;

import io.f1.backend.global.lock.DistributedLock;
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

    @EventListener
    public void handleDisconnectedListener(SessionDisconnectEvent event) {

        Message<?> message = event.getMessage();
        UserPrincipal principal = getSessionUser(message);

        Long userId = principal.getUserId();

        /* 정상 로직 */
        if (!roomService.isUserInAnyRoom(userId)) {
            return;
        }

        Long roomId = roomService.getUserRoomId(userId);

        changeConnectionStateWithLock(userId,roomId);

        taskManager.scheduleDisconnectTask(
                userId,
                () -> {
                    if (ConnectionState.DISCONNECTED.equals(
                            roomService.getPlayerState(userId, roomId))) {
                        roomService.exitIfNotPlaying(roomId, principal);
                    }
                });
    }


    @DistributedLock(prefix = "room", key = "#roomId")
    private void changeConnectionStateWithLock(Long userId, Long roomId){
        roomService.changeConnectedStatus(userId,roomId ,ConnectionState.DISCONNECTED);
    }
}
