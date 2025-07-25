package io.f1.backend.domain.game.event;

import io.f1.backend.domain.game.dto.ChatMessage;
import io.f1.backend.domain.game.model.Room;

public record GameCorrectAnswerEvent(
        Room room, String sessionId, ChatMessage chatMessage, String answer) {}
