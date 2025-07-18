package io.f1.backend.domain.stat.dto;

public record StatWithNickname(
	String nickname,
	long totalGames,
	long winningGames,
	long score
) { }
