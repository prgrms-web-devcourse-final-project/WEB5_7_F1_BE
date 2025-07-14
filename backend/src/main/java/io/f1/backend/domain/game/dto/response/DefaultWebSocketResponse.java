package io.f1.backend.domain.game.dto.response;

import io.f1.backend.domain.game.dto.MessageType;
import io.f1.backend.domain.game.dto.WebSocketDto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DefaultWebSocketResponse<T> implements WebSocketDto<T> {
    private final MessageType type;
    private final T message;
}
