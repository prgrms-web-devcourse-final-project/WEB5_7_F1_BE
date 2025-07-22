package io.f1.backend.domain.game.dto.response;

public record GameResultResponse(String nickname, int score, int totalCorrectCount, int rank) {
}
