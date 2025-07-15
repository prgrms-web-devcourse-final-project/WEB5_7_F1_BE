package io.f1.backend.domain.game.dto.request;

import jakarta.validation.constraints.NotNull;

public record RoomValidationRequest(
        @NotNull(message = "roomId 값은 필수입니다. ") Long roomId,
        @NotNull(message = "비밀번호는 null 값이 아니여야합니다. ") String password) {}
