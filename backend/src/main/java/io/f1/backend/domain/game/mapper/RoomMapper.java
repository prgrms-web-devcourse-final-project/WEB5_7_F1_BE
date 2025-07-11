package io.f1.backend.domain.game.mapper;

import io.f1.backend.domain.game.dto.request.RoomCreateRequest;
import io.f1.backend.domain.game.model.RoomSetting;

public class RoomMapper {

    public static RoomSetting toRoomSetting(RoomCreateRequest request) {
        return new RoomSetting(
                request.roomName(), request.maxUserCount(), request.locked(), request.password());
    }
}
