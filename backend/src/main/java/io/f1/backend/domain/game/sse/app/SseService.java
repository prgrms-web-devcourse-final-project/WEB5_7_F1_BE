package io.f1.backend.domain.game.sse.app;

import io.f1.backend.domain.game.sse.dto.LobbySseEvent;
import io.f1.backend.domain.game.sse.store.SseEmitterRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SseService {

    private final SseEmitterRepository emitterRepository;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(1_800_000L);
        emitterRepository.save(emitter);

        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
            startHeartBeat(emitter);
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
        return emitter;
    }

    public <T> void notifyLobbyUpdate(LobbySseEvent<T> event) {
        for (SseEmitter emitter : emitterRepository.getAll()) {
            try {
                emitter.send(SseEmitter.event().name(event.type()).data(event));
            } catch (IOException e) {
                emitterRepository.remove(emitter);
            }
        }
    }

    private void startHeartBeat(SseEmitter emitter) {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                emitter.send(SseEmitter.event().name("heartbeat").data("sse-alive"));
            } catch (IOException e) {
                emitterRepository.remove(emitter);
            }
        }, 5, 60, TimeUnit.SECONDS);
    }
}
