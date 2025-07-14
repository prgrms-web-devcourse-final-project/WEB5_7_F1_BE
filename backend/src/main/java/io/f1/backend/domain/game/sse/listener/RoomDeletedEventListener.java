package io.f1.backend.domain.game.sse.listener;

import static io.f1.backend.domain.game.sse.mapper.SseMapper.*;

import io.f1.backend.domain.game.event.RoomDeletedEvent;
import io.f1.backend.domain.game.sse.app.SseService;
import io.f1.backend.domain.game.sse.dto.LobbySseEvent;
import io.f1.backend.domain.game.sse.dto.RoomDeletedPayload;

import lombok.RequiredArgsConstructor;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

@RequiredArgsConstructor
public class RoomDeletedEventListener {

    private final SseService sseService;

    @Async
    @EventListener
    public void roomDelete(RoomDeletedEvent event) {
        LobbySseEvent<RoomDeletedPayload> sseEvent = fromRoomDeleted(event);
        sseService.notifyLobbyUpdate(sseEvent);
    }
}
