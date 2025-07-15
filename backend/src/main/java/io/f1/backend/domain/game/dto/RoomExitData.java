package io.f1.backend.domain.game.dto;

import io.f1.backend.domain.game.dto.response.PlayerListResponse;
import io.f1.backend.domain.game.dto.response.SystemNoticeResponse;
import lombok.Builder;
import lombok.Getter;


@Getter
public class RoomExitData {

    private final String destination;
    private final PlayerListResponse playerListResponses;
    private final SystemNoticeResponse systemNoticeResponse;
    private final boolean removedRoom;

    @Builder
    public RoomExitData(String destination, PlayerListResponse playerListResponses,
        SystemNoticeResponse systemNoticeResponse, boolean removedRoom) {
        this.destination = destination;
        this.playerListResponses = playerListResponses;
        this.systemNoticeResponse = systemNoticeResponse;
        this.removedRoom = removedRoom;
    }

}
