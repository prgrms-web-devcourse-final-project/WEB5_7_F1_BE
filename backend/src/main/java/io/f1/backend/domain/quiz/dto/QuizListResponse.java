package io.f1.backend.domain.quiz.dto;

public record QuizListResponse(
        Long quizId,
        String title,
        String description,
        String creatorNickname,
        int numberOfQuestion,
        String thumbnailUrl) {}
