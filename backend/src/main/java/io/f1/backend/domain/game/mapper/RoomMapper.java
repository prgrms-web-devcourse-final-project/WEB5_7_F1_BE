package io.f1.backend.domain.game.mapper;

import io.f1.backend.domain.game.dto.request.RoomCreateRequest;
import io.f1.backend.domain.game.dto.response.RoomResponse;
import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.game.model.RoomSetting;
import io.f1.backend.domain.quiz.entity.Quiz;

public class RoomMapper {

    public static RoomSetting toRoomSetting(RoomCreateRequest request) {
        return new RoomSetting(
                request.roomName(), request.maxUserCount(), request.locked(), request.password());
    }

    public static RoomResponse toRoomResponse(Room room, Quiz quiz) {
        return new RoomResponse(
                room.getId(),
                room.getRoomSetting().roomName(),
                room.getRoomSetting().maxUserCount(),
                room.getPlayerSessionMap().size(),
                room.getRoomSetting().locked(),
                room.getState().name(),
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.getCreator().getNickname(),
                quiz.getQuestions().size(),
                quiz.getThumbnailUrl());
    }
}
