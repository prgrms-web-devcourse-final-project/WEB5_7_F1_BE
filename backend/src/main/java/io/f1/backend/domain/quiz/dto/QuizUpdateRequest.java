package io.f1.backend.domain.quiz.dto;

import io.f1.backend.domain.question.dto.QuestionUpdateRequest;

import io.f1.backend.global.validation.TrimmedSize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizUpdateRequest {

    @TrimmedSize(min = 2, max = 30)
    @NotBlank(message = "퀴즈 제목을 설정해주세요.")
    private String title;

    @TrimmedSize(min = 10, max = 50)
    @NotBlank(message = "퀴즈 설명을 적어주세요.")
    private String description;

    @Size(min = 10, max = 80, message = "문제는 최소 10개, 최대 80개로 정해주세요.")
    private List<QuestionUpdateRequest> questions;

}
