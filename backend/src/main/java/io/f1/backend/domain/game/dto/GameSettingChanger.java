package io.f1.backend.domain.game.dto;

import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.quiz.app.QuizService;

public interface GameSettingChanger {
    boolean change(Room room, QuizService quizService);
}
