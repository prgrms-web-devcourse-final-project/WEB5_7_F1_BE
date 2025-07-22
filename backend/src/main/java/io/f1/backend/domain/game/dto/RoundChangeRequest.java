package io.f1.backend.domain.game.dto;

import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.quiz.app.QuizService;
import io.f1.backend.domain.quiz.entity.Quiz;

import java.util.Objects;

public record RoundChangeRequest(Integer round) implements GameSettingChanger {

    @Override
    public boolean change(Room room, QuizService quizService) {
        if (Objects.equals(room.getGameSetting().getRound(), round)) {
            return false; // 동일하면 무시
        }

        Quiz quiz = quizService.findQuizById(room.getGameSetting().getQuizId());
        int questionSize = quiz.getQuestions().size();

        room.getGameSetting().changeRound(round, questionSize);
        return true;
    }
}
