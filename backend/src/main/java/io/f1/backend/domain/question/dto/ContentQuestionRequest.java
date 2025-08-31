package io.f1.backend.domain.question.dto;

import io.f1.backend.domain.question.entity.ContentQuestion;
import io.f1.backend.domain.question.entity.Question;
import io.f1.backend.domain.quiz.entity.Quiz;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ContentQuestionRequest {
    private String content;
    private String answer;

    public static ContentQuestionRequest of(String content, String answer) {
        return new ContentQuestionRequest(content, answer);
    }

    public ContentQuestion toContentQuestion(Question question) {
        return new ContentQuestion(question, content);
    }

    public Question toQuestion(Quiz quiz) {
        return new Question(quiz, answer);
    }
}
