package io.f1.backend.domain.game.sse.listener;

import static io.f1.backend.domain.game.sse.mapper.SseMapper.*;

import io.f1.backend.domain.game.event.RoomUpdatedEvent;
import io.f1.backend.domain.game.sse.app.SseService;
import io.f1.backend.domain.game.sse.dto.LobbySseEvent;
import io.f1.backend.domain.game.sse.dto.RoomUpdatedPayload;

import lombok.RequiredArgsConstructor;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoomUpdatedEventListener {

    private final SseService sseService;

    @Async
    @EventListener
    public void roomUpdate(RoomUpdatedEvent event) {
        LobbySseEvent<RoomUpdatedPayload> sseEvent = fromRoomUpdated(event);
        sseService.notifyLobbyUpdate(sseEvent);
    }
}
