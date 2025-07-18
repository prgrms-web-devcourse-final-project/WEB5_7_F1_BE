package io.f1.backend.domain.stat.dto;

public record StatResponse(
        long rank, String nickname, long totalGames, long winningGames, long score) {}
