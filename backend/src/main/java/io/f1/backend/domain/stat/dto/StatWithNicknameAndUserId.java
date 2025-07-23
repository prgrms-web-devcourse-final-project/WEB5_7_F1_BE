package io.f1.backend.domain.stat.dto;

public record StatWithNicknameAndUserId(
        long userId, String nickname, long totalGames, long winningGames, long score) {}
