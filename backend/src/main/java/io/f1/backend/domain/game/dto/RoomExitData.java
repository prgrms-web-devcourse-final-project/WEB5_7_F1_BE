package io.f1.backend.domain.game.dto;

import io.f1.backend.domain.game.dto.response.PlayerListResponse;
import io.f1.backend.domain.game.dto.response.SystemNoticeResponse;

public record RoomExitData(String destination, PlayerListResponse playerListResponses,
                           SystemNoticeResponse systemNoticeResponse,
                           boolean removedRoom) {

}
