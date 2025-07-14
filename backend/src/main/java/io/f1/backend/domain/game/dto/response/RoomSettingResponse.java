package io.f1.backend.domain.game.dto.response;

public record RoomSettingResponse(String roomName, int maxUserCount, int currentUserCount) {}
