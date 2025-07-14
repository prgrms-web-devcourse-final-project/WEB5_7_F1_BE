package io.f1.backend.domain.question.mapper;

import io.f1.backend.domain.question.dto.QuestionRequest;
import io.f1.backend.domain.question.entity.Question;
import io.f1.backend.domain.quiz.entity.Quiz;

public class QuestionMapper {

    public static Question questionRequestToQuestion(Quiz quiz, QuestionRequest questionRequest) {
        return new Question(quiz, questionRequest.getAnswer());
    }
}
