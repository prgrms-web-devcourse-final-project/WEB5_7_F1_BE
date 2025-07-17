package io.f1.backend.domain.game.dto;

import io.f1.backend.domain.game.dto.response.PlayerListResponse;

public record PlayerReadyData(String destination, PlayerListResponse response) {}
