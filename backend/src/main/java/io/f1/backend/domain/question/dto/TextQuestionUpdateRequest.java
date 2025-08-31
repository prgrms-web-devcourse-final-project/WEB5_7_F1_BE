package io.f1.backend.domain.question.dto;

import io.f1.backend.global.validation.TrimmedSize;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TextQuestionUpdateRequest {

    private Long id;

    @TrimmedSize(min = 5, max = 30)
    @NotBlank(message = "문제를 입력해주세요.")
    private String content;

    @TrimmedSize(min = 1, max = 30)
    @NotBlank(message = "정답을 입력해주세요.")
    private String answer;
}
