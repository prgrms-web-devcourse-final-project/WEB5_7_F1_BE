package io.f1.backend.domain.question.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionRequest {

    @NotBlank(message = "문제를 입력해주세요.")
    private String content;

    @NotBlank(message = "정답을 입력해주세요.")
    private String answer;
}
