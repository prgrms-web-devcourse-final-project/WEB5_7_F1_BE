package io.f1.backend.domain.game.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.f1.backend.domain.game.dto.request.RoomValidationRequest;
import io.f1.backend.domain.game.model.GameSetting;
import io.f1.backend.domain.game.model.Player;
import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.game.model.RoomSetting;
import io.f1.backend.domain.game.store.RoomRepository;
import io.f1.backend.domain.game.websocket.MessageSender;
import io.f1.backend.domain.quiz.app.QuizService;
import io.f1.backend.domain.user.dto.UserPrincipal;
import io.f1.backend.domain.user.entity.User;
import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.util.SecurityUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
@ExtendWith(MockitoExtension.class)
class RoomServiceTests {

    private RoomService roomService;

    @Mock private RoomRepository roomRepository;
    @Mock private QuizService quizService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private MessageSender messageSender;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // @Mock 어노테이션이 붙은 필드들을 초기화합니다.
        roomService = new RoomService(quizService, roomRepository, eventPublisher, messageSender);

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void afterEach() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("enterRoom_동시성_테스트")
    void enterRoom_synchronized() throws Exception {
        Long roomId = 1L;
        Long quizId = 1L;
        Long playerId = 1L;
        int maxUserCount = 5;
        String password = "123";
        boolean locked = true;

        Room room = createRoom(roomId, playerId, quizId, password, maxUserCount, locked);

        when(roomRepository.findRoom(roomId)).thenReturn(Optional.of(room));

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        RoomValidationRequest roomValidationRequest = new RoomValidationRequest(roomId, password);
        for (int i = 1; i <= threadCount; i++) {
            User user = createUser(i);

            executorService.submit(
                    () -> {
                        try {
                            SecurityUtils.setAuthentication(user);
                            roomService.enterRoom(roomValidationRequest);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            SecurityContextHolder.clearContext();
                            countDownLatch.countDown();
                        }
                    });
        }
        countDownLatch.await();
        assertThat(room.getUserIdSessionMap()).hasSize(room.getRoomSetting().maxUserCount());
    }

    @Test
    @DisplayName("exitRoom_동시성_테스트")
    void exitRoom_synchronized() throws Exception {
        Long roomId = 1L;
        Long quizId = 1L;
        Long playerId = 1L;
        int maxUserCount = 5;
        String password = "123";
        boolean locked = true;

        Room room = createRoom(roomId, playerId, quizId, password, maxUserCount, locked);

        int threadCount = 10;

        List<Player> players = new ArrayList<>();
        for (int i = 1; i <= threadCount; i++) {
            Long id = i + 1L;
            String nickname = "nickname " + i;

            Player player = new Player(id, nickname);
            players.add(player);
        }
        Player host = players.getFirst();
        room.updateHost(host);

        for (int i = 1; i <= threadCount; i++) {
            String sessionId = "sessionId" + i;
            Player player = players.get(i - 1);
            room.getPlayerSessionMap().put(sessionId, player);
            room.getUserIdSessionMap().put(player.getId(), sessionId);
        }

        log.info("room.getPlayerSessionMap().size() = {}", room.getPlayerSessionMap().size());

        when(roomRepository.findRoom(roomId)).thenReturn(Optional.of(room));

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        for (int i = 1; i <= threadCount; i++) {
            String sessionId = "sessionId" + i;
            User user = createUser(i);
            executorService.submit(
                    () -> {
                        try {
                            UserPrincipal principal =
                                    new UserPrincipal(user, Collections.emptyMap());
                            SecurityUtils.setAuthentication(user);
                            log.info("room.getHost().getId() = {}", room.getHost().getId());
                            roomService.exitRoom(roomId, sessionId, principal);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            SecurityContextHolder.clearContext();
                            countDownLatch.countDown();
                        }
                    });
        }
        countDownLatch.await();
        assertThat(room.getUserIdSessionMap()).hasSize(1);
    }

    @Test
    @DisplayName("정상연결이라면_아무것도 안함")
    void shouldDoNothing_whenUserIsPendingSession() throws Exception {

        Long roomId = 1L;
        Long playerId = 1L;

        String sessionId = "abc";

        when(roomRepository.findRoom(roomId)).thenReturn(Optional.of(room));
        when(room.isPendingSession(playerId)).thenReturn(true);

        // when
        roomService.manageSession(roomId, sessionId, playerId);

        // then
        verify(room, never()).reconnectSession(any(), any());

    }

    @Test
    @DisplayName("manageSession_enter안된유저라면_예외발생")
    void shouldThrowException_whenUserHasNotEnteredRoom() throws Exception {
        // given
        Long roomId = 1L;
        Long userId = 1L;
        String sessionId = "abc";

        when(roomRepository.findRoom(roomId)).thenReturn(Optional.of(room));
        when(room.isPendingSession(userId)).thenReturn(false);
        Map<Long, String> userIdSessionMap = new HashMap<>();
        when(room.getUserIdSessionMap()).thenReturn(userIdSessionMap);

        // when & then
        assertThrows(CustomException.class,
            () -> roomService.manageSession(roomId, sessionId, userId));
    }


    @Test
    @DisplayName("manageSession_재연결이면_reconnectSession_호출됨")
    void shouldReconnectSession_whenUserReentersWithNewSession() {
        // given
        Long roomId = 1L;
        Long userId = 1L;
        String sessionId = "newSessionId";

        when(roomRepository.findRoom(roomId)).thenReturn(Optional.of(room));
        when(room.isPendingSession(userId)).thenReturn(false);

        Map<Long, String> userIdSessionMap = new HashMap<>();
        userIdSessionMap.put(userId, "oldSession");

        when(room.getUserIdSessionMap()).thenReturn(userIdSessionMap);

        // when
        roomService.manageSession(roomId, sessionId, userId);

        // then
        verify(room).reconnectSession(userId, sessionId);
    }

    private Room createRoom(
            Long roomId,
            Long playerId,
            Long quizId,
            String password,
            int maxUserCount,
            boolean locked) {
        RoomSetting roomSetting = new RoomSetting("방제목", maxUserCount, locked, password);
        GameSetting gameSetting = new GameSetting(quizId, 10, 60);
        Player host = new Player(playerId, "nickname");

        return new Room(roomId, roomSetting, gameSetting, host);
    }

    private User createUser(int i) {
        Long userId = i + 1L;
        String provider = "provider +" + i;
        String providerId = "providerId" + i;
        LocalDateTime lastLogin = LocalDateTime.now();

        User user =
                User.builder()
                        .provider(provider)
                        .providerId(providerId)
                        .lastLogin(lastLogin)
                        .build();
        user.setId(userId);

        return user;
    }


}
