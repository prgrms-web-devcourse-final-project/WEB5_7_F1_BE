package io.f1.backend.domain.game.dto.request;

import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.game.websocket.MessageSender;
import io.f1.backend.domain.quiz.app.QuizService;
import org.springframework.context.ApplicationEventPublisher;

public interface GameSettingChanger {

    boolean change(Room room, QuizService quizService);

    default void afterChange(Room room, MessageSender messageSender, ApplicationEventPublisher eventPublisher, QuizService quizService) {

    }
}
