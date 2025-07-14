package io.f1.backend.domain.game.dto.response;

public record QuizResponse(
        Long quizId, String title, String description, String thumbnailUrl, int numberOfQuestion) {}
