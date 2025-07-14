package io.f1.backend.domain.game.sse.app;

import io.f1.backend.domain.game.sse.dto.LobbySseEvent;
import io.f1.backend.domain.game.sse.store.SseEmitterRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class SseService {

    private final SseEmitterRepository emitterRepository;

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(30_000L);
        emitterRepository.save(emitter);

        try {
            // emitter 정상 전송확인 메시지
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException e) {
            // emitter send() 호출 시 예외 처리
            emitterRepository.remove(emitter);
        }
        return emitter;
    }

    // 로비로 SSE 메시지를 쏘기위한 메서드
    public <T> void notifyLobbyUpdate(LobbySseEvent<T> event) {
        for (SseEmitter emitter : emitterRepository.getAll()) {
            try {
                emitter.send(SseEmitter.event()
                    .name(event.type())
                    .data(event)
                );
            } catch (IOException e) {
                emitterRepository.remove(emitter);
            }
        }
    }
}
