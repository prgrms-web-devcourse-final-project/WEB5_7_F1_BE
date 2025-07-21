package io.f1.backend.domain.game.websocket.Service;

import io.f1.backend.domain.game.app.RoomService;
import io.f1.backend.domain.game.model.ConnectionState;
import io.f1.backend.domain.user.dto.UserPrincipal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class SessionService {

    private final RoomService roomService;
    private final Map<String, Long> sessionIdUser = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionIdRoom = new ConcurrentHashMap<>();
    private final Map<Long, String> userIdSession = new ConcurrentHashMap<>();
    private final Map<Long, String> userIdLatestSession = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void addSession(String sessionId, Long userId) {
        sessionIdUser.put(sessionId, userId);
        userIdSession.put(userId, sessionId);
    }

    public void addRoomId(Long roomId, String sessionId) {
        sessionIdRoom.put(sessionId, roomId);
    }

    public void handleUserReconnect(Long roomId, String newSessionId, Long userId) {

        String oldSessionId = userIdLatestSession.get(userId);
        /* room 재연결 대상인지 아닌지 판별 */
        if (roomService.isReconnectTarget(roomId, oldSessionId)) {
            roomService.reconnectSession(roomId, oldSessionId, newSessionId);
        }
    }

    public void handleUserDisconnect(String sessionId, UserPrincipal principal) {

        Long roomId = sessionIdRoom.get(sessionId);
        roomService.changeConnectedStatus(roomId, sessionId, ConnectionState.DISCONNECTED);

        // 5초 뒤 실행
        scheduler.schedule(() -> {
            ConnectionState playerConnectionState = roomService.getPlayerConnectionState(roomId,
                sessionId);

            if (playerConnectionState == ConnectionState.DISCONNECTED) {
                roomService.exitIfNotPlaying(roomId, sessionId, principal);
            } else {
                roomService.notifyIfReconnected(roomId, principal);
            }
            userIdLatestSession.remove(principal.getUserId());
        }, 5, TimeUnit.SECONDS);
    }

    public void removeSession(String sessionId,Long userId) {
        sessionIdUser.remove(sessionId);
        sessionIdRoom.remove(sessionId);
        userIdSession.remove(userId);

        userIdLatestSession.put(userId, sessionId);
    }

}
