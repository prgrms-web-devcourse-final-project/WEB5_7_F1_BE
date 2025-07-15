package io.f1.backend.domain.game.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RoomCreateRequest(
        @NotBlank(message = "방 제목은 필수입니다.") String roomName,
        @NotNull(message = "인원 수 입력은 필수입니다.")
                @Min(value = 2, message = "방 인원 수는 최소 2명입니다.")
                @Max(value = 8, message = "방 인원 수는 최대 8명 입니다.")
                Integer maxUserCount,
        @NotNull String password,
        @NotNull Boolean locked) {}
