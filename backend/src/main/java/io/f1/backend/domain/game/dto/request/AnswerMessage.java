package io.f1.backend.domain.game.dto.request;

import java.time.Instant;

public record AnswerMessage(String nickname, String message, Instant timestamp, Long questionId) {}
