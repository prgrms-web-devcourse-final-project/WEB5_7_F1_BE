package io.f1.backend.domain.question.dto;

public record QuestionUpdateRequest(Long id, String content, String answer) {}
