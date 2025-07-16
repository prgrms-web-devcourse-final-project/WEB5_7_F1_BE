package io.f1.backend.domain.game.dto;

import io.f1.backend.domain.game.dto.response.GameStartResponse;

public record GameStartData(String destination, GameStartResponse gameStartResponse) {}
