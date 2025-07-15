package io.f1.backend.domain.game.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import io.f1.backend.domain.game.dto.request.RoomValidationRequest;
import io.f1.backend.domain.game.model.GameSetting;
import io.f1.backend.domain.game.model.Player;
import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.game.model.RoomSetting;
import io.f1.backend.domain.game.store.RoomRepository;
import io.f1.backend.domain.quiz.app.QuizService;
import io.f1.backend.domain.user.entity.User;
import io.f1.backend.global.util.SecurityUtils;

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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@ExtendWith(MockitoExtension.class)
class RoomServiceTests {

    private RoomService roomService;

    @Mock private RoomRepository roomRepository;
    @Mock private QuizService quizService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // @Mock 어노테이션이 붙은 필드들을 초기화합니다.
        roomService = new RoomService(quizService, roomRepository, eventPublisher);

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
        String password = "123";

        RoomSetting roomSetting = new RoomSetting("방제목", 5, true, password);
        GameSetting gameSetting = new GameSetting(quizId, 10, 60);
        Player host = new Player(playerId, "닉네임");

        Room room = new Room(roomId, roomSetting, gameSetting, host);

        when(roomRepository.findRoom(roomId)).thenReturn(Optional.of(room));

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        RoomValidationRequest roomValidationRequest = new RoomValidationRequest(roomId, password);
        for (int i = 1; i <= threadCount; i++) {
            Long userId = i + 1L;
            String provider = "provider +" + i;
            String providerId = "providerId" + i;
            LocalDateTime lastLogin = LocalDateTime.now();
            executorService.submit(
                    () -> {
                        try {
                            User user =
                                    User.builder()
                                            .provider(provider)
                                            .provider(providerId)
                                            .lastLogin(lastLogin)
                                            .build();

                            user.setId(userId);

                            SecurityUtils.setAuthentication(user);

                            roomService.enterRoom(roomValidationRequest);
                        } catch (Exception e) {
                            // e.printStackTrace();
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
        int threadCount = 10;

        String password = "123";

        RoomSetting roomSetting = new RoomSetting("방제목", 5, true, password);
        GameSetting gameSetting = new GameSetting(quizId, 10, 60);

        List<Player> players = new ArrayList<>();
        for (int i = 1; i <= threadCount; i++) {
            Long id = i + 1L;
            String nickname = "nickname " + i;

            Player player = new Player(id, nickname);
            players.add(player);
        }
        Player host = players.getFirst();
        Room room = new Room(roomId, roomSetting, gameSetting, host);

        for (int i = 1; i <= threadCount; i++) {
            String sessionId = "sessionId" + i;
            Player player = players.get(i - 1);
            room.getPlayerSessionMap().put(sessionId, player);
            room.getUserIdSessionMap().put(player.getId(), sessionId);
        }

        log.info("room.getPlayerSessionMap().size() = {}", room.getPlayerSessionMap().size());

        when(roomRepository.findRoom(roomId)).thenReturn(Optional.of(room));
        doNothing().when(roomRepository).removeRoom(roomId);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        for (int i = 1; i <= threadCount; i++) {
            Long userId = i + 1L;
            String sessionId = "sessionId" + i;
            String provider = "provider +" + i;
            String providerId = "providerId" + i;
            LocalDateTime lastLogin = LocalDateTime.now();
            executorService.submit(
                    () -> {
                        try {
                            User user =
                                    User.builder()
                                            .provider(provider)
                                            .provider(providerId)
                                            .lastLogin(lastLogin)
                                            .build();
                            user.setId(userId);
                            SecurityUtils.setAuthentication(user);

                            log.info("userId = {}", userId);
                            log.info("room.getHost().getId() = {}", room.getHost().getId());
                            roomService.exitRoom(roomId, sessionId);
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
}
