package io.f1.backend.domain.quiz.dto;

import io.f1.backend.domain.question.dto.QuestionUpdateRequest;

import java.util.List;

public record QuizUpdateRequest(
        String title, String description, List<QuestionUpdateRequest> questions) {}
