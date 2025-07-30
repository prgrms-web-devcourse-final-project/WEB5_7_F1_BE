package io.f1.backend.domain.game.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.f1.backend.domain.game.dto.ChatMessage;
import io.f1.backend.domain.game.event.GameCorrectAnswerEvent;
import io.f1.backend.domain.game.model.GameSetting;
import io.f1.backend.domain.game.model.Player;
import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.game.model.RoomSetting;
import io.f1.backend.domain.game.model.RoomState;
import io.f1.backend.domain.game.websocket.MessageSender;
import io.f1.backend.domain.question.entity.Question;
import io.f1.backend.domain.user.dto.UserPrincipal;
import io.f1.backend.domain.user.entity.User;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
@ExtendWith(MockitoExtension.class)
class ChatServiceTests {

    private ChatService chatService;

    @Mock private RoomService roomService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private MessageSender messageSender;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // @Mock 어노테이션이 붙은 필드들을 초기화합니다.

        chatService = new ChatService(roomService, messageSender, eventPublisher);

        SecurityContextHolder.clearContext();
    }


    @Test
    @DisplayName("정답이 아닐 때 이벤트가 발행되지 않는다.")
    void noEventWhenIncorrect() throws Exception {

        // given
        Long roomId = 1L;
        ChatMessage wrongMessage = new ChatMessage("뜨거운제티", "오답", Instant.now());

        Room room = mock(Room.class);
        Question question = mock(Question.class);
        User user = createUser(1);
        UserPrincipal userPrincipal = new UserPrincipal(user, Collections.emptyMap());

        given(roomService.findRoom(roomId)).willReturn(room);
        given(room.isPlaying()).willReturn(true);
        given(room.getCurrentQuestion()).willReturn(question);
        given(question.getAnswer()).willReturn("정답");

        // when
        chatService.chat(roomId, userPrincipal, wrongMessage);

        // then
        verify(eventPublisher, never()).publishEvent(any(GameCorrectAnswerEvent.class));
    }

    @Test
    @DisplayName("정답일 때 GameCorrectAnswerEvent가 발행된다. ")
    void EventPublishedWhenCorrect() throws Exception {

        // given
        Long roomId = 1L;
        ChatMessage answer = new ChatMessage("뜨거운제티", "정답", Instant.now());

        Room room = mock(Room.class);
        Question question = mock(Question.class);
        User user = createUser(1);
        UserPrincipal userPrincipal = new UserPrincipal(user, Collections.emptyMap());

        given(roomService.findRoom(roomId)).willReturn(room);
        given(room.isPlaying()).willReturn(true);
        given(room.getCurrentQuestion()).willReturn(question);
        given(question.getAnswer()).willReturn("정답");
        given(room.compareAndSetAnsweredFlag(false, true)).willReturn(true);

        // when
        chatService.chat(roomId, userPrincipal, answer);

        // then
        verify(eventPublisher, times(1)).publishEvent(any(GameCorrectAnswerEvent.class));

    }

    @Test
    @DisplayName("동시에 여러 명의 사용자가 채팅을 보냈을 때, 한 명만 정답인정")
    void onlyOneCorrectPlayerWhenConcurrentCorrectAnswers() throws Exception {

        // given
        Long roomId = 1L;
        Long quizId = 1L;
        Long playerId = 1L;
        int maxUserCount = 5;
        String password = "123";
        boolean locked = true;
        String correctAnswer = "정답";

        Room room = createRoom(roomId, playerId, quizId, password, maxUserCount, locked);
        Question question = mock(Question.class);
        room.updateRoomState(RoomState.PLAYING);
        room.increaseCurrentRound();
        room.updateQuestions(Collections.singletonList(question));

        // room이 실제 객체이므로, RoomService만 mock으로 대체
        given(roomService.findRoom(roomId)).willReturn(room);
        given(question.getAnswer()).willReturn(correctAnswer);

        int userCount = 8;
        ExecutorService executor = Executors.newFixedThreadPool(userCount);

        CountDownLatch countDownLatch = new CountDownLatch(userCount);

        ChatMessage msg = new ChatMessage("닉네임", correctAnswer, Instant.now());

        for (int i = 0; i < userCount; i++) {
            final int idx = i;
            executor.submit(() -> {
                try {
                    User user = createUser(idx);
                    UserPrincipal principal = new UserPrincipal(user, Collections.emptyMap());
                    chatService.chat(roomId, principal, msg);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();


        // then: 이벤트는 단 1번만 발행돼야 함
        ArgumentCaptor<GameCorrectAnswerEvent> captor = ArgumentCaptor.forClass(GameCorrectAnswerEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());

        GameCorrectAnswerEvent event = captor.getValue();
        log.info("정답 인정된 유저 ID : {}", event.userId());
        assertThat(event.userId()).isBetween(0L, 7L);

        verify(eventPublisher, times(1)).publishEvent(any(GameCorrectAnswerEvent.class));
        assertThat(room.getAnswered().get()).isTrue();
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

        try {
            Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, userId);
        } catch (Exception e) {
            throw new RuntimeException("ID 설정 실패", e);
        }

        return user;
    }
  
}