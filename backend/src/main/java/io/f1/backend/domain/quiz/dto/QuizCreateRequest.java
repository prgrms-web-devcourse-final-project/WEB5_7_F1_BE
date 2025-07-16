package io.f1.backend.domain.quiz.dto;

import io.f1.backend.domain.question.dto.QuestionRequest;
import io.f1.backend.domain.quiz.entity.QuizType;
import io.f1.backend.global.validation.TrimmedSize;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizCreateRequest {

    @TrimmedSize(min = 2, max = 30)
    @NotBlank(message = "퀴즈 제목을 설정해주세요.")
    private String title;

    @NotNull(message = "퀴즈 종류를 선택해주세요.")
    private QuizType quizType;

    @TrimmedSize(min = 10, max = 50)
    @NotBlank(message = "퀴즈 설명을 적어주세요.")
    private String description;

    @Size(min = 10, max = 80, message = "문제는 최소 10개, 최대 80개로 정해주세요.")
    private List<QuestionRequest> questions;
}
