package io.f1.backend.domain.game.mapper;

import io.f1.backend.domain.game.dto.request.RoomCreateRequest;
import io.f1.backend.domain.game.dto.response.GameSettingResponse;
import io.f1.backend.domain.game.dto.response.PlayerListResponse;
import io.f1.backend.domain.game.dto.response.PlayerResponse;
import io.f1.backend.domain.game.dto.response.QuizResponse;
import io.f1.backend.domain.game.dto.response.RoomResponse;
import io.f1.backend.domain.game.dto.response.RoomSettingResponse;
import io.f1.backend.domain.game.model.GameSetting;
import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.game.model.RoomSetting;
import io.f1.backend.domain.quiz.entity.Quiz;

import java.util.List;

public class RoomMapper {

    public static RoomSetting toRoomSetting(RoomCreateRequest request) {
        return new RoomSetting(
                request.roomName(), request.maxUserCount(), request.locked(), request.password());
    }

    public static RoomSettingResponse toRoomSettingResponse(Room room) {
        return new RoomSettingResponse(
                room.getRoomSetting().roomName(),
                room.getRoomSetting().maxUserCount(),
                room.getPlayerSessionMap().size());
    }

    public static GameSettingResponse toGameSettingResponse(
            GameSetting gameSetting, QuizResponse quiz) {
        return new GameSettingResponse(gameSetting.getRound(), gameSetting.getTimeLimit(), quiz);
    }

    public static PlayerListResponse toPlayerListResponse(Room room) {
        List<PlayerResponse> playerResponseList =
                room.getPlayerSessionMap().values().stream()
                        .map(player -> new PlayerResponse(player.getNickname(), false))
                        .toList();

        return new PlayerListResponse(room.getHost().getNickname(), playerResponseList);
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
