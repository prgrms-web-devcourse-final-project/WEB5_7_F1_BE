package io.f1.backend.domain.game.websocket;

import static io.f1.backend.domain.game.app.RoomService.ROOM_LOCK_PREFIX;
import static io.f1.backend.domain.game.app.RoomService.USER_LOCK_PREFIX;
import static io.f1.backend.domain.game.websocket.WebSocketUtils.getUserDestination;

import io.f1.backend.domain.game.app.RoomService;
import io.f1.backend.domain.game.dto.MessageType;
import io.f1.backend.domain.game.dto.response.HeartbeatResponse;
import io.f1.backend.domain.user.dto.UserPrincipal;
import io.f1.backend.global.lock.LockExecutor;

import lombok.RequiredArgsConstructor;

import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class HeartbeatMonitor {

    private static final String DIRECTION = "serverToClient";
    private static final int MAX_MISSED_HEARTBEATS = 3;
    private static final long HEARTBEAT_CHECK_INTERVAL_MS = 15000L;

    private final Map<String, Integer> missedPongCounter = new ConcurrentHashMap<>();

    private final MessageSender messageSender;
    private final RoomService roomService;
    private final SimpUserRegistry simpUserRegistry;
    private final LockExecutor lockExecutor;

    @Scheduled(fixedDelay = HEARTBEAT_CHECK_INTERVAL_MS)
    public void monitorClientHeartbeat() {
        /* user 없으면 skip */
        if (simpUserRegistry.getUserCount() == 0) {
            return;
        }

        simpUserRegistry
                .getUsers()
                .forEach(
                        user ->
                                user.getSessions()
                                        .forEach(session -> handleSessionHeartbeat(user, session)));
    }

    private void handleSessionHeartbeat(SimpUser user, SimpSession session) {
        String sessionId = session.getId();

        /* ping */
        messageSender.sendPersonal(
                getUserDestination(),
                MessageType.HEARTBEAT,
                new HeartbeatResponse(DIRECTION),
                user.getName());

        missedPongCounter.merge(sessionId, 1, Integer::sum);
        int missedCnt = missedPongCounter.get(sessionId);

        /* max_missed_heartbeats 이상 pong 이 안왔을때 - disconnect 처리 */
        if (missedCnt >= MAX_MISSED_HEARTBEATS) {

            Principal principal = user.getPrincipal();

            if (principal instanceof UsernamePasswordAuthenticationToken token
                    && token.getPrincipal() instanceof UserPrincipal userPrincipal) {

                Long userId = userPrincipal.getUserId();
                Long roomId = roomService.getRoomIdByUserIdWithLock(userId);

                lockExecutor.executeWithLock(
                        USER_LOCK_PREFIX,
                        userId,
                        () -> {
                            lockExecutor.executeWithLock(
                                    ROOM_LOCK_PREFIX,
                                    roomId,
                                    () -> {
                                        roomService.disconnectOrExitRoom(roomId, userPrincipal);
                                    });
                        });
            }
            cleanSession(sessionId);
        }
    }

    public void resetMissedPongCount(String sessionId) {
        missedPongCounter.put(sessionId, 0);
    }

    public void cleanSession(String sessionId) {
        missedPongCounter.remove(sessionId);
    }
}
