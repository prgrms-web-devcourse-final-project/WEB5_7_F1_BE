package io.f1.backend.domain.game.websocket.controller;

import static io.f1.backend.domain.game.websocket.WebSocketUtils.getSessionId;

import io.f1.backend.domain.game.websocket.HeartbeatMonitor;

import lombok.RequiredArgsConstructor;

import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class HeartbeatController {

    private final HeartbeatMonitor heartbeatMonitor;

    @MessageMapping("/heartbeat/pong")
    public void handlePong(Message<?> message) {
        String sessionId = getSessionId(message);

        heartbeatMonitor.resetMissedPongCount(sessionId);
    }
}
