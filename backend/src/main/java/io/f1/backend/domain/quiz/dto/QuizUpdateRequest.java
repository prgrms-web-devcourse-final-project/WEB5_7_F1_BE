package io.f1.backend.domain.quiz.dto;

import io.f1.backend.global.validation.TrimmedSize;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class QuizUpdateRequest {
    @TrimmedSize(min = 2, max = 30)
    @NotBlank(message = "퀴즈 제목을 설정해주세요.")
    private String title;

    @TrimmedSize(min = 10, max = 50)
    @NotBlank(message = "퀴즈 설명을 적어주세요.")
    private String description;
}
