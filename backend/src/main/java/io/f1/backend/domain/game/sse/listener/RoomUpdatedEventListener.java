package io.f1.backend.domain.game.sse.listener;

import io.f1.backend.domain.game.event.RoomUpdatedEvent;
import io.f1.backend.domain.game.sse.app.SseService;
import io.f1.backend.domain.game.sse.dto.LobbySseEvent;
import io.f1.backend.domain.game.sse.dto.RoomUpdatedPayload;
import io.f1.backend.domain.game.sse.mapper.SseMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

@RequiredArgsConstructor
public class RoomUpdatedEventListener {

    private final SseService sseService;

    @Async
    @EventListener
    public void roomUpdate(RoomUpdatedEvent event) {
        LobbySseEvent<RoomUpdatedPayload> sseEvent = SseMapper.fromRoomUpdated(event);
        sseService.notifyLobbyUpdate(sseEvent);
    }
}
