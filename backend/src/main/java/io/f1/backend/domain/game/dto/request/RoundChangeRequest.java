package io.f1.backend.domain.game.dto.request;

import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.game.websocket.MessageSender;
import io.f1.backend.domain.quiz.app.QuizService;
import io.f1.backend.domain.quiz.entity.Quiz;

public record RoundChangeRequest(int round) implements GameSettingChanger {

    @Override
    public boolean change(Room room, QuizService quizService) {
        if (room.getRound() == round) {
            return false; // 동일하면 무시
        }

        Quiz quiz = quizService.getQuizWithQuestionsById(room.getQuizId());
        int questionSize = quiz.getQuestions().size();

        room.changeRound(round, questionSize);
        return true;
    }

    @Override
    public void afterChange(Room room, MessageSender messageSender) {
        // 고유한 후처리 동작 없음
    }
}
