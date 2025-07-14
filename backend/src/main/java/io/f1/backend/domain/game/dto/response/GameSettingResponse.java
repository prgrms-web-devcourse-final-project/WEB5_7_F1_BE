package io.f1.backend.domain.game.dto.response;

public record GameSettingResponse(int round, int timeLimit, QuizResponse quiz) {}
