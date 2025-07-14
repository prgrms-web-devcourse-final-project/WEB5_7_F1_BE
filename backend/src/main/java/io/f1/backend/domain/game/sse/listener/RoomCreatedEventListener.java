package io.f1.backend.domain.game.sse.listener;

import io.f1.backend.domain.game.event.RoomCreatedEvent;
import io.f1.backend.domain.game.sse.app.SseService;
import io.f1.backend.domain.game.sse.dto.LobbySseEvent;
import io.f1.backend.domain.game.sse.dto.RoomCreatedPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import static io.f1.backend.domain.game.sse.mapper.SseMapper.*;

@Component
@RequiredArgsConstructor
public class RoomCreatedEventListener {

    private final SseService sseService;

    @Async
    @EventListener
    public void roomCreate(RoomCreatedEvent event) {
        LobbySseEvent<RoomCreatedPayload> sseEvent = fromRoomCreated(event);
        sseService.notifyLobbyUpdate(sseEvent);
    }

}
