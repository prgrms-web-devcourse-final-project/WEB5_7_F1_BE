package io.f1.backend.domain.quiz.dto;

import io.f1.backend.domain.question.dto.TextQuestionUpdateRequest;

import jakarta.validation.constraints.Size;

import lombok.Getter;

import java.util.List;

@Getter
public class TextQuizUpdateRequest extends QuizUpdateRequest {
    @Size(min = 10, max = 80, message = "문제는 최소 10개, 최대 80개로 정해주세요.")
    private List<TextQuestionUpdateRequest> questions;

    public TextQuizUpdateRequest(
            String title, String description, List<TextQuestionUpdateRequest> questions) {
        super(title, description);
        this.questions = questions;
    }
}
