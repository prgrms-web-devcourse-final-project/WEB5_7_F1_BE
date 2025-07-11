package io.f1.backend.domain.game.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RoomCreateRequest(@NotNull(message = "방 제목은 필수입니다.") String roomName,
                                @NotNull(message = "인원 수 입력은 필수입니다.") @Min(value = 2, message = "방 인원 수는 최소 2명 이상이어야합니다.") Integer maxUserCount,
                                String password,
                                boolean locked) {

}
