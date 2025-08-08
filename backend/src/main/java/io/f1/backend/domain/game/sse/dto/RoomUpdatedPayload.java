package io.f1.backend.domain.game.sse.dto;

public record RoomUpdatedPayload(
        Long roomId,
        int currentUserCount,
        String roomState,
        String quizTitle,
        String description,
        String creator,
        int numberOfQuestions,
        String thumbnailUrl) {}
