package io.f1.backend.domain.game.app;

import static io.f1.backend.domain.game.dto.MessageType.QUESTION_RESULT;
import static io.f1.backend.domain.game.dto.MessageType.QUESTION_START;
import static io.f1.backend.domain.game.websocket.WebSocketUtils.getDestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.f1.backend.domain.game.dto.ChatMessage;
import io.f1.backend.domain.game.dto.MessageType;
import io.f1.backend.domain.game.dto.RoomEventType;
import io.f1.backend.domain.game.dto.SystemNoticeMessage;
import io.f1.backend.domain.game.event.GameCorrectAnswerEvent;
import io.f1.backend.domain.game.event.GameTimeoutEvent;
import io.f1.backend.domain.game.model.GameSetting;
import io.f1.backend.domain.game.model.Player;
import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.game.model.RoomSetting;
import io.f1.backend.domain.game.model.RoomState;
import io.f1.backend.domain.game.websocket.DisconnectTaskManager;
import io.f1.backend.domain.game.websocket.MessageSender;
import io.f1.backend.domain.question.entity.Question;
import io.f1.backend.domain.quiz.app.QuizService;
import io.f1.backend.domain.stat.app.StatService;
import io.f1.backend.domain.user.dto.UserPrincipal;
import io.f1.backend.domain.user.entity.User;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@ExtendWith(MockitoExtension.class)
class GameFlowTests {

    private ChatService chatService;
    private GameService gameService;
    private TestRoomService testRoomService;

    @Mock private QuizService quizService;
    @Mock private StatService statService;
    @Mock private TimerService timerService;
    @Mock private MessageSender messageSender;
    @Mock private DisconnectTaskManager disconnectTaskManager;

    private Room room;
    private Question question;

    @BeforeEach
    void setUp() {
        question = mock(Question.class);

        ApplicationEventPublisher eventPublisher =
                new ApplicationEventPublisher() {
                    @Override
                    public void publishEvent(Object event) {
                        if (event instanceof GameCorrectAnswerEvent e) {
                            gameService.onCorrectAnswer(e);
                        } else if (event instanceof GameTimeoutEvent e) {
                            gameService.onTimeout(e);
                        }
                    }
                };

        testRoomService = new TestRoomService();
        chatService = new ChatService(testRoomService, messageSender, eventPublisher);
        gameService =
                new GameService(
                        statService,
                        quizService,
                        testRoomService,
                        timerService,
                        messageSender,
                        null,
                        eventPublisher);
    }

    @Test
    @DisplayName("정답 채팅이 타임아웃보다 먼저 도착하면 정답으로 인정된다")
    void correctChatBeforeTimeout_shouldPreferChat() throws Exception {

        // given
        Long roomId = 1L;
        Long quizId = 1L;
        Long playerId = 1L;
        int maxUserCount = 5;
        String password = "123";
        boolean locked = true;
        String correctAnswer = "정답";

        room = createRoom(roomId, playerId, quizId, password, maxUserCount, locked);
        when(question.getAnswer()).thenReturn(correctAnswer);

        room.updateRoomState(RoomState.PLAYING);
        room.increaseCurrentRound();
        room.updateQuestions(Collections.singletonList(question));
        room.getAnswered().set(false);

        testRoomService.register(room); // Room 등록

        User user = createUser(1);
        UserPrincipal principal = new UserPrincipal(user, Collections.emptyMap());
        ChatMessage answer = new ChatMessage("뜨거운제티", correctAnswer, Instant.now());

        CountDownLatch latch = new CountDownLatch(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // when
        // 채팅으로 정답 -> AtomicBoolean (false -> true)
        executor.submit(
                () -> {
                    try {
                        chatService.chat(roomId, principal, answer);
                        log.info("채팅으로 정답! 현재 시간 : {}", Instant.now());
                    } finally {
                        latch.countDown();
                    }
                });

        // 그 찰나에 timeout 발생 -> AtomicBoolean compareAndSet 때문에 return! 실행 안됨.
        executor.submit(
                () -> {
                    try {
                        Thread.sleep(100); // 살짝 늦게 타임아웃 발생
                        gameService.onTimeout(new GameTimeoutEvent(room));
                        log.info("그 찰나에 타임아웃 발생! 현재 시간 : {}", Instant.now());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                });

        boolean done = latch.await(3, java.util.concurrent.TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
        assertThat(done).isTrue();

        // then

        verify(messageSender, atMostOnce())
                .sendBroadcast(eq(getDestination(roomId)), eq(QUESTION_RESULT), any());
        verify(messageSender, atMostOnce())
                .sendBroadcast(eq(getDestination(roomId)), eq(QUESTION_START), any());

        ArgumentCaptor<SystemNoticeMessage> noticeCaptor =
                ArgumentCaptor.forClass(SystemNoticeMessage.class);

        verify(messageSender, atMost(1))
                .sendBroadcast(
                        eq(getDestination(roomId)),
                        eq(MessageType.SYSTEM_NOTICE),
                        noticeCaptor.capture());

        // SYSTEM_NOTICE가 TIMEOUT 내용이었는지 확인
        if (!noticeCaptor.getAllValues().isEmpty()) {
            SystemNoticeMessage message = noticeCaptor.getValue();
            assertThat(message.getMessage()).isNotEqualTo(RoomEventType.TIMEOUT);
        }
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
        String provider = "provider" + i;
        String providerId = "providerId" + i;
        LocalDateTime lastLogin = LocalDateTime.now();

        User user =
                User.builder()
                        .provider(provider)
                        .providerId(providerId)
                        .lastLogin(lastLogin)
                        .build();

        try {
            Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, userId);
        } catch (Exception e) {
            throw new RuntimeException("ID 설정 실패", e);
        }

        return user;
    }

    // 내부 클래스: 테스트용 RoomService
    static class TestRoomService extends RoomService {
        private final Map<Long, Room> rooms = new ConcurrentHashMap<>();

        public TestRoomService() {
            super(null, null, null, null, null, null, null);
        }

        @Override
        public Room findRoom(Long roomId) {
            return rooms.get(roomId);
        }

        public void register(Room room) {
            rooms.put(room.getId(), room);
        }
    }
}
