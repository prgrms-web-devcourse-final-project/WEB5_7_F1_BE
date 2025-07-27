package io.f1.backend.domain.game.dto.request;

import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.quiz.app.QuizService;

public record RoundChangeRequest(int round) implements GameSettingChanger {

    @Override
    public boolean change(Room room, QuizService quizService) {
        if (room.getRound() == round) {
            return false; // 동일하면 무시
        }

        Long questionsCount = quizService.getQuestionsCount(room.getQuizId());

        room.changeRound(round, questionsCount.intValue());
        return true;
    }
}
