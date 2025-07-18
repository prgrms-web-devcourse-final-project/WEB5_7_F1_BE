package io.f1.backend.domain.game.dto.response;

import io.f1.backend.domain.game.dto.Rank;

import java.util.List;

public record RankUpdateResponse(List<Rank> rank) {}
