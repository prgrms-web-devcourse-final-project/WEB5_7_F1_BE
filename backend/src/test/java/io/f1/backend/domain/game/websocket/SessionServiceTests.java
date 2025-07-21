package io.f1.backend.domain.game.websocket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.f1.backend.domain.game.app.RoomService;
import io.f1.backend.domain.game.model.ConnectionState;
import io.f1.backend.domain.game.websocket.Service.SessionService;
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
    @DisplayName("handleUserReconnect: 재연결 대상일 경우 RoomService.reconnectSession이 호출되는지 확인")
    void handleUserReconnect_shouldReconnectIfTarget() {
        // 준비: 이전 세션 정보 설정
        ReflectionTestUtils.invokeMethod(
                sessionService, "addSession", sessionId1, userId1); // userIdLatestSession에 값이 들어가게
        ReflectionTestUtils.setField(
                sessionService, "userIdLatestSession", Map.of(userId1, sessionId1));

        // 메서드 호출
        User user = new User("provider", "providerId", LocalDateTime.now());
        user.setId(userId1);
        UserPrincipal principal = new UserPrincipal(user, new HashMap<>());
        sessionService.handleUserReconnect(roomId1, sessionId2, principal);

        // RoomService.reconnectSession이 올바른 인자로 호출되었는지 검증
        verify(roomService, times(1)).reconnectSession(roomId1, sessionId1, sessionId2, principal);
        verify(roomService, never()).changeConnectedStatus(any(), any(), any()); // 다른 메서드 호출 안됨 확인
    }

    @Test
    @DisplayName("handleUserReconnect: 재연결 대상이 아닐 경우 RoomService.reconnectSession이 호출되지 않는지 확인")
    void handleUserReconnect_shouldNotReconnectIfNotTarget() {
        // 준비: 이전 세션 정보 설정
        ReflectionTestUtils.invokeMethod(sessionService, "addSession", sessionId1, userId1);
        ReflectionTestUtils.setField(
                sessionService, "userIdLatestSession", Map.of(userId1, sessionId1));

        // RoomService.isExit -> true 반환하도록 모의 설정
        when(roomService.isExit(sessionId1, roomId1)).thenReturn(true);

        User user = new User("provider", "providerId", LocalDateTime.now());
        user.setId(userId1);
        UserPrincipal principal = new UserPrincipal(user, new HashMap<>());
        // 메서드 호출
        sessionService.handleUserReconnect(roomId1, sessionId2, principal);

        // RoomService.reconnectSession이 호출되지 않았는지 검증
        verify(roomService, never())
                .reconnectSession(anyLong(), anyString(), anyString(), any(UserPrincipal.class));
    }

    @Test
    @DisplayName("handleUserDisconnect: 연결 끊김 상태이고 재연결되지 않았으면 exitIfNotPlaying 호출")
    void handleUserDisconnect_shouldExitIfNotPlayingIfDisconnected() throws InterruptedException {
        // 준비: 맵에 더미 데이터 추가
        sessionService.addRoomId(roomId1, sessionId1);
        sessionService.addSession(sessionId1, userId1); // ✅ 추가 필요

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
        assertEquals(sessionId1, userIdLatestSession.get(principal.getUserId()));
    }

    @Test
    @DisplayName("removeSession: 세션 관련 정보가 올바르게 제거되고 userIdLatestSession에 업데이트되는지 확인")
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
        assertTrue(userIdLatestSession.containsKey(userId1));
        assertEquals(sessionId1, userIdLatestSession.get(userId1));
    }
}
