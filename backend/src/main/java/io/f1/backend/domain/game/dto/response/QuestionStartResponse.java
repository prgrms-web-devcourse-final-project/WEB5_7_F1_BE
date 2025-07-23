package io.f1.backend.domain.game.dto.response;

import java.time.Instant;

public record QuestionStartResponse(Long questionId, int round, Instant timestamp) {}
