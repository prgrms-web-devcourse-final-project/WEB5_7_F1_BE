package io.f1.backend.domain.game.dto;

import io.f1.backend.domain.game.dto.response.GameStartResponse;
import io.f1.backend.domain.game.dto.response.QuestionStartResponse;

public record GameStartData(
        GameStartResponse gameStartResponse, QuestionStartResponse questionStartResponse) {}
