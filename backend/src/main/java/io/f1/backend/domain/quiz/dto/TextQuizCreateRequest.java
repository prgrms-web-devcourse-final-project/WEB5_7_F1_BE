package io.f1.backend.domain.quiz.dto;

import io.f1.backend.domain.question.dto.TextQuestionRequest;
import io.f1.backend.domain.quiz.entity.QuizType;

import jakarta.validation.constraints.Size;

import lombok.Getter;

import java.util.List;

@Getter
public class TextQuizCreateRequest extends QuizCreateRequest {
    @Size(min = 10, max = 80, message = "문제는 최소 10개, 최대 80개로 정해주세요.")
    private List<TextQuestionRequest> questions;

    public TextQuizCreateRequest(
            String title, String description, List<TextQuestionRequest> questions) {
        super(title, QuizType.TEXT, description);
        this.questions = questions;
    }
}
