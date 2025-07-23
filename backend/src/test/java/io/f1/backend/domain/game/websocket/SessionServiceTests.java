package io.f1.backend.domain.game.websocket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.f1.backend.domain.game.app.RoomService;
import io.f1.backend.domain.game.model.ConnectionState;
import io.f1.backend.domain.game.websocket.service.SessionService;
import io.f1.backend.domain.user.dto.UserPrincipal;
import io.f1.backend.domain.user.entity.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

@ExtendWith(MockitoExtension.class)
class SessionServiceTests {

    @Mock private RoomService roomService;

    @InjectMocks private SessionService sessionService;

    // 테스트를 위한 더미 데이터
    private String sessionId1 = "session1";
    private String sessionId2 = "session2";
    private Long userId1 = 100L;
    private Long userId2 = 200L;
    private Long roomId1 = 1L;
    private Long roomId2 = 2L;

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(sessionService, "sessionIdUser", new ConcurrentHashMap<>());
        ReflectionTestUtils.setField(sessionService, "sessionIdRoom", new ConcurrentHashMap<>());
        ReflectionTestUtils.setField(sessionService, "userIdSession", new ConcurrentHashMap<>());
        ReflectionTestUtils.setField(
                sessionService, "userIdLatestSession", new ConcurrentHashMap<>());

        ReflectionTestUtils.setField(
                sessionService, "scheduler", Executors.newScheduledThreadPool(2));
    }

    @Test
    @DisplayName("addSession: 세션과 사용자 ID가 올바르게 추가되는지 확인")
    void addSession_shouldAddSessionAndUser() {
        sessionService.addSession(sessionId1, userId1);

        Map<String, Long> sessionIdUser =
                (Map<String, Long>) ReflectionTestUtils.getField(sessionService, "sessionIdUser");
        Map<Long, String> userIdSession =
                (Map<Long, String>) ReflectionTestUtils.getField(sessionService, "userIdSession");

        assertEquals(1, sessionIdUser.size());
        assertEquals(1, userIdSession.size());

        // 값의 정확성 확인
        assertEquals(userId1, sessionIdUser.get(sessionId1));
        assertEquals(sessionId1, userIdSession.get(userId1));
    }

    @Test
    @DisplayName("addRoomId: 세션과 룸 ID가 올바르게 추가되는지 확인")
    void addRoomId_shouldAddSessionAndRoom() {
        sessionService.addRoomId(roomId1, sessionId1);

        Map<String, Long> sessionIdRoom =
                (Map<String, Long>) ReflectionTestUtils.getField(sessionService, "sessionIdRoom");

        assertEquals(1, sessionIdRoom.size());
        assertEquals(roomId1, sessionIdRoom.get(sessionId1));
    }

    @Test
    @DisplayName("handleUserDisconnect: 연결 끊김 상태이고 재연결되지 않았으면 exitIfNotPlaying 호출")
    void handleUserDisconnect_shouldExitIfNotPlayingIfDisconnected() throws InterruptedException {
        // 준비: 맵에 더미 데이터 추가
        sessionService.addRoomId(roomId1, sessionId1);
        sessionService.addSession(sessionId1, userId1);

        User user = new User("provider", "providerId", LocalDateTime.now());
        user.setId(userId1);
        UserPrincipal principal = new UserPrincipal(user, new HashMap<>());

        // disconnect 호출
        sessionService.handleUserDisconnect(sessionId1, principal);

        Thread.sleep(5100); // 5초 + 여유 시간

        // verify: roomService.changeConnectedStatus가 호출되었는지 확인
        verify(roomService, times(1))
                .changeConnectedStatus(roomId1, sessionId1, ConnectionState.DISCONNECTED);

        // verify: roomService.exitIfNotPlaying이 호출되었는지 확인
        verify(roomService, times(1)).exitIfNotPlaying(eq(roomId1), eq(sessionId1), eq(principal));

        Map<Long, String> userIdLatestSession =
                (Map<Long, String>)
                        ReflectionTestUtils.getField(sessionService, "userIdLatestSession");
        assertEquals(null, userIdLatestSession.get(principal.getUserId()));
    }

    @Test
    @DisplayName("removeSession: 세션 관련 정보가 올바르게 제거되고 userIdLatestSession에 삭제 확인")
    void removeSession_shouldRemoveAndPutLatestSession() {
        // 준비: 초기 데이터 추가
        sessionService.addSession(sessionId1, userId1);
        sessionService.addRoomId(roomId1, sessionId1);

        // removeSession 호출
        sessionService.removeSession(sessionId1, userId1);

        // 맵의 크기 및 내용 확인
        Map<String, Long> sessionIdUser =
                (Map<String, Long>) ReflectionTestUtils.getField(sessionService, "sessionIdUser");
        Map<String, Long> sessionIdRoom =
                (Map<String, Long>) ReflectionTestUtils.getField(sessionService, "sessionIdRoom");
        Map<Long, String> userIdSession =
                (Map<Long, String>) ReflectionTestUtils.getField(sessionService, "userIdSession");
        Map<Long, String> userIdLatestSession =
                (Map<Long, String>)
                        ReflectionTestUtils.getField(sessionService, "userIdLatestSession");

        // 제거되었는지 확인
        assertFalse(sessionIdUser.containsKey(sessionId1));
        assertFalse(sessionIdRoom.containsKey(sessionId1));
        assertFalse(userIdSession.containsKey(userId1));

        // userIdLatestSession에 업데이트되었는지 확인
        assertFalse(userIdLatestSession.containsKey(userId1));
    }

    @Test
    @DisplayName("handleUserDisconnect: 연결 끊김 시 userIdLatestSession에 이전 세션 ID가 저장되는지 확인")
    void handleUserDisconnect_shouldStoreOldSessionIdInLatestSession() {
        // given
        sessionService.addSession(sessionId1, userId1); // 유저의 현재 활성 세션
        sessionService.addRoomId(roomId1, sessionId1);

        User user = new User("provider", "providerId", LocalDateTime.now());
        user.setId(userId1);
        UserPrincipal principal = new UserPrincipal(user, new HashMap<>());

        // when
        sessionService.handleUserDisconnect(sessionId1, principal);

        // then
        Map<Long, String> userIdLatestSession =
                (Map<Long, String>)
                        ReflectionTestUtils.getField(sessionService, "userIdLatestSession");

        assertTrue(userIdLatestSession.containsKey(userId1));
        assertEquals(sessionId1, userIdLatestSession.get(userId1));

        // 재연결 상태 변경 검증
        verify(roomService, times(1))
                .changeConnectedStatus(roomId1, sessionId1, ConnectionState.DISCONNECTED);
    }

    @Test
    @DisplayName(
            "handleUserDisconnect 후 5초 내 재연결: userIdLatestSession이 정리되고 exitIfNotPlaying이 호출되지 않음")
    void handleUserDisconnect_reconnectWithin5Seconds_shouldCleanLatestSession()
            throws InterruptedException {
        // given
        sessionService.addSession(sessionId1, userId1); // 초기 세션
        sessionService.addRoomId(roomId1, sessionId1);

        User user = new User("provider", "providerId", LocalDateTime.now());
        user.setId(userId1);
        UserPrincipal principal = new UserPrincipal(user, new HashMap<>());

        sessionService.handleUserDisconnect(
                sessionId1, principal); // 세션1 끊김, userIdLatestSession에 세션1 저장

        // 5초 타이머가 실행되기 전에 새로운 세션으로 재연결 시도 (userIdSession 업데이트)
        sessionService.addSession(sessionId2, userId1); // userId1의 새 세션은 sessionId2
        sessionService.addRoomId(roomId1, sessionId2); // 새 세션도 룸에 추가

        // when (5초가 경과했다고 가정)
        Thread.sleep(5100);

        // then
        Map<Long, String> userIdLatestSession =
                (Map<Long, String>)
                        ReflectionTestUtils.getField(sessionService, "userIdLatestSession");
        Map<String, Long> sessionIdUser =
                (Map<String, Long>) ReflectionTestUtils.getField(sessionService, "sessionIdUser");
        Map<String, Long> sessionIdRoom =
                (Map<String, Long>) ReflectionTestUtils.getField(sessionService, "sessionIdRoom");
        Map<Long, String> userIdSession =
                (Map<Long, String>) ReflectionTestUtils.getField(sessionService, "userIdSession");

        // userIdLatestSession은 정리되어야 함
        assertFalse(userIdLatestSession.containsKey(userId1));
        assertNull(userIdLatestSession.get(userId1));

        // roomService.exitIfNotPlaying은 호출되지 않아야 함 (재연결 성공했으므로)
        verify(roomService, never())
                .exitIfNotPlaying(anyLong(), anyString(), any(UserPrincipal.class));

        // 세션 관련 맵들이 올바르게 정리되었는지 확인
        // sessionId1에 대한 정보는 모두 삭제되어야 함
        assertFalse(sessionIdUser.containsKey(sessionId1)); // sessionId1은 sessionIdUser에서 삭제
        assertFalse(sessionIdRoom.containsKey(sessionId1)); // sessionId1은 sessionIdRoom에서 삭제

        // userIdSession은 sessionId2로 업데이트되어 있어야 함
        assertTrue(userIdSession.containsKey(userId1));
        assertEquals(sessionId2, userIdSession.get(userId1));

        // sessionId2에 대한 정보는 남아있어야 함
        assertTrue(sessionIdUser.containsKey(sessionId2));
        assertEquals(userId1, sessionIdUser.get(sessionId2));
        assertTrue(sessionIdRoom.containsKey(sessionId2));
        assertEquals(roomId1, sessionIdRoom.get(sessionId2));
    }
}
