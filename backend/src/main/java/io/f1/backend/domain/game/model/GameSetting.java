package io.f1.backend.domain.game.model;

import io.f1.backend.domain.game.dto.request.TimeLimit;
import io.f1.backend.domain.quiz.entity.Quiz;
import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.exception.errorcode.GameErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GameSetting {

    private Long quizId;
    private Integer round;
    private int timeLimit;

    public void changeQuiz(Quiz quiz) {
        quizId = quiz.getId();
        round = quiz.getQuestions().size(); // 라운드를 바꾼 퀴즈의 문제 수로 동기화
    }

    public void changeTimeLimit(TimeLimit timeLimit) {
        this.timeLimit = timeLimit.getValue();
    }

    public void changeRound(int round, int questionsCount) {
        if (round > questionsCount) {
            throw new CustomException(GameErrorCode.ROUND_EXCEEDS_QUESTION_COUNT);
        }
        this.round = round;
    }
}
