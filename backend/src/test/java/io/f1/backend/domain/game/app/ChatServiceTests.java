package io.f1.backend.domain.game.app;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.f1.backend.domain.game.dto.ChatMessage;
import io.f1.backend.domain.game.event.GameCorrectAnswerEvent;
import io.f1.backend.domain.game.model.GameSetting;
import io.f1.backend.domain.game.model.Player;
import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.game.model.RoomSetting;
import io.f1.backend.domain.game.websocket.MessageSender;
import io.f1.backend.domain.question.entity.Question;
import io.f1.backend.domain.user.entity.User;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
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
        String sessionId = "session123";
        ChatMessage wrongMessage = new ChatMessage("nick", "오답", Instant.now());

        Room room = mock(Room.class);
        Question question = mock(Question.class);

        given(roomService.findRoom(roomId)).willReturn(room);
        given(room.isPlaying()).willReturn(true);
        given(room.getCurrentQuestion()).willReturn(question);
        given(question.getAnswer()).willReturn("정답");

        // when
        chatService.chat(roomId, sessionId, wrongMessage);

        // then
        verify(eventPublisher, never()).publishEvent(any(GameCorrectAnswerEvent.class));
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