package io.f1.backend.domain.quiz.dto;

import java.util.List;

import io.f1.backend.domain.question.dto.ImageQuestionRequest;
import io.f1.backend.domain.quiz.entity.QuizType;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ImageQuizCreateRequest extends QuizCreateRequest{
    @Size(min = 10, max = 80, message = "문제는 최소 10개, 최대 80개로 정해주세요.")
    private List<ImageQuestionRequest> questions;

    public ImageQuizCreateRequest(String title, String description, List<ImageQuestionRequest> questions) {
        super(title, QuizType.IMAGE, description);
        this.questions = questions;
    }
}