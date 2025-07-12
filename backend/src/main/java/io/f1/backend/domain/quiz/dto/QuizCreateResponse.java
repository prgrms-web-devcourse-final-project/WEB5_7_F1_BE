package io.f1.backend.domain.quiz.dto;

import io.f1.backend.domain.quiz.entity.QuizType;

public record QuizCreateResponse(
        Long id,
        String title,
        QuizType quizType,
        String description,
        String thumbnailUrl,
        Long creatorId) {}
