package io.f1.backend.domain.game.websocket;

import static io.f1.backend.domain.game.websocket.WebSocketUtils.getUserDestination;

import io.f1.backend.domain.game.app.RoomService;
import io.f1.backend.domain.game.dto.MessageType;
import io.f1.backend.domain.game.dto.response.HeartbeatResponse;
import io.f1.backend.domain.user.dto.UserPrincipal;
import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Slf4j
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

    @Scheduled(fixedDelay = HEARTBEAT_CHECK_INTERVAL_MS)
    public void monitorClientHeartbeat() {
        /* user 없으면 skip */
        if (simpUserRegistry.getUserCount() == 0) {
            return;
        }

        simpUserRegistry.getUsers().forEach(user ->
            user.getSessions().forEach(session -> handleSessionHeartbeat(user, session)));

    }

    private void handleSessionHeartbeat(SimpUser user, SimpSession session) {
        String sessionId = session.getId();

        /* pong */
        messageSender.sendPersonal(getUserDestination(),
            MessageType.HEARTBEAT, new HeartbeatResponse(DIRECTION), user.getName());

        missedPongCounter.merge(sessionId, 1, Integer::sum);
        int missedCnt = missedPongCounter.get(sessionId);

        /* max_missed_heartbeats 이상 pong 이 안왔을때 - disconnect 처리 */
        if (missedCnt >= MAX_MISSED_HEARTBEATS) {

            Principal principal = user.getPrincipal();

            if (principal instanceof UsernamePasswordAuthenticationToken token &&
                token.getPrincipal() instanceof UserPrincipal userPrincipal) {

                Long userId = userPrincipal.getUserId();
                Long roomId = roomService.getRoomIdByUserId(userId);

                roomService.disconnectOrExitRoom(roomId, userPrincipal);
            }
            missedPongCounter.remove(sessionId);
        }
    }

    public void resetMissedPongCount(String sessionId) {
        missedPongCounter.put(sessionId, 0);
    }

    public void cleanSession(String sessionId) {
        missedPongCounter.remove(sessionId);
    }

}
