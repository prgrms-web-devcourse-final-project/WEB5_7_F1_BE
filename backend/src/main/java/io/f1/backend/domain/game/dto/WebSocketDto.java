package io.f1.backend.domain.game.dto;

public interface WebSocketDto<T> {
    MessageType getType();

    T getMessage();
}
