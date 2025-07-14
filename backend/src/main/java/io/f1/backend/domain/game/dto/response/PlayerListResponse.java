package io.f1.backend.domain.game.dto.response;

import java.util.List;

public record PlayerListResponse(String host, List<PlayerResponse> players) {}
