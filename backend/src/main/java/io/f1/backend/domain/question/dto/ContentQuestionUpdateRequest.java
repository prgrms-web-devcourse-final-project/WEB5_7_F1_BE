package io.f1.backend.domain.question.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ContentQuestionUpdateRequest {
    private Long id;
    private String content;
    private String answer;

    public static ContentQuestionUpdateRequest of(Long id, String content, String answer) {
        return new ContentQuestionUpdateRequest(id, content, answer);
    }
}
