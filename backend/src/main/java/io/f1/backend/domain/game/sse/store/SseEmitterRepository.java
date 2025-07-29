package io.f1.backend.domain.game.sse.store;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class SseEmitterRepository {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public void save(SseEmitter emitter) {
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(error -> emitters.remove(emitter));
    }

    public void remove(SseEmitter emitter) {
        emitters.remove(emitter);
    }

    public List<SseEmitter> getAll() {
        return emitters;
    }
}
