package io.f1.backend.domain.game.dto.request;

import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.game.websocket.MessageSender;
import io.f1.backend.domain.quiz.app.QuizService;

public interface GameSettingChanger {

    boolean change(Room room, QuizService quizService);

    void afterChange(Room room, MessageSender messageSender);
}
