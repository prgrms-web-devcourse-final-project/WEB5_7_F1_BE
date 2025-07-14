package io.f1.backend.domain.question.mapper;

import io.f1.backend.domain.question.entity.Question;
import io.f1.backend.domain.question.entity.TextQuestion;

public class TextQuestionMapper {

    public static TextQuestion questionRequestToTextQuestion(Question question, String content) {
        return new TextQuestion(question, content);
    }
}
