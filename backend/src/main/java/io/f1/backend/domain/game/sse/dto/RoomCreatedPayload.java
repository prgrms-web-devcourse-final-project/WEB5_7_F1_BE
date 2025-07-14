package io.f1.backend.domain.game.sse.dto;

public record RoomCreatedPayload(
        Long roomId,
        String roomName,
        Integer maxUserCount,
        int currentUserCount,
        boolean locked,
        String roomState,
        String quizTitle,
        String description,
        String creator,
        Integer numberOfQuestion,
        String thumbnailUrl) {}
