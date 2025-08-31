package io.f1.backend.domain.question.mapper;

import io.f1.backend.domain.question.dto.ContentQuestionRequest;
import io.f1.backend.domain.question.entity.Question;
import io.f1.backend.domain.quiz.entity.Quiz;

public class QuestionMapper {

    public static Question questionRequestToQuestion(
            Quiz quiz, ContentQuestionRequest questionRequest) {
        return new Question(quiz, questionRequest.getAnswer());
    }
}
