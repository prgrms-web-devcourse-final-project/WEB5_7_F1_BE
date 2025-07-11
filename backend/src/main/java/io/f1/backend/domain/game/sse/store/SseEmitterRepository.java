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
        // 연결종료 객체정리
        emitter.onCompletion(
                () -> {
                    emitters.remove(emitter);
                });
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(error -> emitters.remove(emitter));
    }

    // 연결 종료 객체 정리
    public void remove(SseEmitter emitter) {
        emitters.remove(emitter);
    }

    // 브로드캐스팅
    public List<SseEmitter> getAll() {
        return emitters;
    }
}
