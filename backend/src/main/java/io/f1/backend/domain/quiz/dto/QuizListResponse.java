package io.f1.backend.domain.quiz.dto;

import io.f1.backend.domain.quiz.entity.QuizType;

public record QuizListResponse(
        Long quizId,
        String title,
        String description,
        QuizType quizType,
        String creatorNickname,
        int numberOfQuestion,
        String thumbnailUrl) {}
