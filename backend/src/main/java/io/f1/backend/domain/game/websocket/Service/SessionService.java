package io.f1.backend.domain.game.websocket.Service;

import io.f1.backend.domain.game.app.RoomService;
import io.f1.backend.domain.game.model.ConnectionState;
import io.f1.backend.domain.user.dto.UserPrincipal;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

        if (userIdLatestSession.get(userId) != null) {
            String oldSessionId = userIdLatestSession.get(userId);
            /* room 재연결 대상인지 아닌지 판별 */
            if (roomService.isReconnectTarget(roomId, oldSessionId)) {
                roomService.reconnectSession(roomId, oldSessionId, newSessionId);
            }
        }
    }

    public void handleUserDisconnect(String sessionId, UserPrincipal principal) {

        Long roomId = sessionIdRoom.get(sessionId);

        /* 정상 동작*/
        if (roomService.isExit(sessionId, roomId)) {
            removeSession(sessionId, roomId);
            return;
        }

        Long userId = principal.getUserId();

        roomService.changeConnectedStatus(roomId, sessionId, ConnectionState.DISCONNECTED);

        // 5초 뒤 실행
        scheduler.schedule(
                () -> {
                    if (userIdSession.get(userId).equals(sessionId)) {
                        roomService.exitIfNotPlaying(roomId, sessionId, principal);
                    } else {
                        roomService.notifyIfReconnected(roomId, principal);
                    }
                    userIdLatestSession.remove(principal.getUserId());
                    removeSession(sessionId, roomId);
                },
                5,
                TimeUnit.SECONDS);
    }

    public void removeSession(String sessionId, Long userId) {
        sessionIdUser.remove(sessionId);
        sessionIdRoom.remove(sessionId);
        userIdSession.remove(userId);

        userIdLatestSession.put(userId, sessionId);
    }
}
