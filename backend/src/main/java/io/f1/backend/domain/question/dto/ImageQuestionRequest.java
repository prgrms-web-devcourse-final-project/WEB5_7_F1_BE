package io.f1.backend.domain.question.dto;

import io.f1.backend.global.validation.TrimmedSize;

import jakarta.validation.constraints.NotBlank;

public record ImageQuestionRequest(
        @TrimmedSize(min = 1, max = 30) @NotBlank(message = "정답을 입력해주세요.") String answer) {}
