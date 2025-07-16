package io.f1.backend.domain.game.dto;

import java.time.Instant;

public record ChatMessage ( String nickname, String message, Instant timestamp) {

}
