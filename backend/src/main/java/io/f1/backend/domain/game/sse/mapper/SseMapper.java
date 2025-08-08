package io.f1.backend.domain.game.sse.mapper;

import io.f1.backend.domain.game.event.RoomCreatedEvent;
import io.f1.backend.domain.game.event.RoomDeletedEvent;
import io.f1.backend.domain.game.event.RoomUpdatedEvent;
import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.game.sse.dto.LobbySseEvent;
import io.f1.backend.domain.game.sse.dto.RoomCreatedPayload;
import io.f1.backend.domain.game.sse.dto.RoomDeletedPayload;
import io.f1.backend.domain.game.sse.dto.RoomUpdatedPayload;
import io.f1.backend.domain.game.sse.dto.SseEventType;
import io.f1.backend.domain.quiz.entity.Quiz;

public class SseMapper {

    public static LobbySseEvent<RoomCreatedPayload> fromRoomCreated(RoomCreatedEvent event) {
        Room room = event.room();
        Quiz quiz = event.quiz();
        long questionSize = event.questionSize();
        RoomCreatedPayload payload =
                new RoomCreatedPayload(
                        room.getId(),
                        room.getRoomSetting().roomName(),
                        room.getRoomSetting().maxUserCount(),
                        room.getCurrentUserCnt(),
                        room.getRoomSetting().locked(),
                        room.getState().name(),
                        quiz.getTitle(),
                        quiz.getDescription(),
                        quiz.getCreator().getNickname(),
                        (int) questionSize,
                        quiz.getThumbnailUrl());
        return new LobbySseEvent<>(SseEventType.CREATE.name(), payload);
    }

    public static LobbySseEvent<RoomUpdatedPayload> fromRoomUpdated(RoomUpdatedEvent event) {
        Room room = event.room();
        Quiz quiz = event.quiz();
        long questionSize = event.questionSize();
        RoomUpdatedPayload payload =
                new RoomUpdatedPayload(
                        room.getId(),
                        room.getCurrentUserCnt(),
                        room.getState().name(),
                        quiz.getTitle(),
                        quiz.getDescription(),
                        quiz.getCreator().getNickname(),
                        (int) questionSize,
                        quiz.getThumbnailUrl());
        return new LobbySseEvent<>(SseEventType.UPDATE.name(), payload);
    }

    public static LobbySseEvent<RoomDeletedPayload> fromRoomDeleted(RoomDeletedEvent event) {
        Long roomId = event.roomId();
        RoomDeletedPayload payload = new RoomDeletedPayload(roomId);
        return new LobbySseEvent<>(SseEventType.DELETE.name(), payload);
    }
}
