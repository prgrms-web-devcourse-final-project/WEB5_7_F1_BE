package io.f1.backend.domain.game.model;

public record RoomSetting(String roomName, int maxUserCount, boolean locked, String password) {}
