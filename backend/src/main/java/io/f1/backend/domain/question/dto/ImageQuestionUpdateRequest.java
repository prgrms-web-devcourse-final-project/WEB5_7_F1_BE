package io.f1.backend.domain.question.dto;

import io.f1.backend.global.validation.TrimmedSize;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;

@Getter
public class ImageQuestionUpdateRequest {
    private Long id;

    private boolean imageFile;

    @TrimmedSize(min = 1, max = 30)
    @NotBlank(message = "정답을 입력해주세요.")
    private String answer;

    public boolean hasImageFile() {
        return imageFile;
    }
}
