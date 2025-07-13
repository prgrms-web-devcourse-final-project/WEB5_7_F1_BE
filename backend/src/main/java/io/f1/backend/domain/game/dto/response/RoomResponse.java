package io.f1.backend.domain.game.dto.response;

public record RoomResponse(
        Long roomId,
        String roomName,
        int maxUserCount,
        int currentUserCount,
        boolean locked,
        String roomState,
        String quizTitle,
        String description,
        String creator,
        int numberOfQuestions,
        String thumbnailUrl) {}
