package io.f1.backend.domain.question.mapper;

import io.f1.backend.domain.question.entity.ContentQuestion;
import io.f1.backend.domain.question.entity.Question;

public class ContentQuestionMapper {

    public static ContentQuestion questionRequestToContentQuestion(
            Question question, String content) {
        return new ContentQuestion(question, content);
    }
}
