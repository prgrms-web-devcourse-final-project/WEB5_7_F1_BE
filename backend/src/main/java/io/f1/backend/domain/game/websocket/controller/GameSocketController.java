package io.f1.backend.domain.game.websocket.controller;

import static io.f1.backend.domain.game.websocket.WebSocketUtils.getSessionId;
import static io.f1.backend.domain.game.websocket.WebSocketUtils.getSessionUser;

import io.f1.backend.domain.game.app.ChatService;
import io.f1.backend.domain.game.app.GameService;
import io.f1.backend.domain.game.app.RoomService;
import io.f1.backend.domain.game.dto.ChatMessage;
import io.f1.backend.domain.game.dto.request.DefaultWebSocketRequest;
import io.f1.backend.domain.game.dto.request.QuizChangeRequest;
import io.f1.backend.domain.game.dto.request.RoundChangeRequest;
import io.f1.backend.domain.game.dto.request.TimeLimitChangeRequest;
import io.f1.backend.domain.game.model.ConnectionState;
import io.f1.backend.domain.user.dto.UserPrincipal;

import lombok.RequiredArgsConstructor;

import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class GameSocketController {

    private final RoomService roomService;
    private final GameService gameService;
    private final ChatService chatService;

    @MessageMapping("/room/initializeRoomSocket/{roomId}")
    public void initializeRoomSocket(@DestinationVariable Long roomId, Message<?> message) {
        UserPrincipal principal = getSessionUser(message);

        roomService.initializeRoomSocket(roomId, principal);
        roomService.addSessionRoomId(getSessionId(message), roomId);
    }

    @MessageMapping("/room/reconnect/{roomId}")
    public void reconnect(@DestinationVariable Long roomId, Message<?> message) {

        UserPrincipal principal = getSessionUser(message);
        roomService.changeConnectedStatusWithLock(
                roomId, principal.getUserId(), ConnectionState.CONNECTED);
        roomService.reconnectSendResponseWithLock(roomId, principal);
        roomService.addSessionRoomId(getSessionId(message), roomId);
    }

    @MessageMapping("/room/exit/{roomId}")
    public void exitRoom(@DestinationVariable Long roomId, Message<?> message) {

        UserPrincipal principal = getSessionUser(message);

        roomService.exitRoomWithLock(roomId, principal);
    }

    @MessageMapping("/room/start/{roomId}")
    public void gameStart(@DestinationVariable Long roomId, Message<?> message) {

        UserPrincipal principal = getSessionUser(message);

        gameService.gameStart(roomId, principal);
    }

    @MessageMapping("room/chat/{roomId}")
    public void chat(
            @DestinationVariable Long roomId,
            Message<DefaultWebSocketRequest<ChatMessage>> message) {

        chatService.chat(roomId, getSessionUser(message), message.getPayload().getMessage());
    }

    @MessageMapping("/room/ready/{roomId}")
    public void playerReady(@DestinationVariable Long roomId, Message<?> message) {

        gameService.handlePlayerReady(roomId, getSessionUser(message));
    }

    @MessageMapping("/room/quiz/{roomId}")
    public void quizChange(
            @DestinationVariable Long roomId,
            Message<DefaultWebSocketRequest<QuizChangeRequest>> message) {
        UserPrincipal principal = getSessionUser(message);
        gameService.changeGameSetting(roomId, principal, message.getPayload().getMessage());
    }

    @MessageMapping("/room/time-limit/{roomId}")
    public void timeLimitChange(
            @DestinationVariable Long roomId,
            Message<DefaultWebSocketRequest<TimeLimitChangeRequest>> message) {
        UserPrincipal principal = getSessionUser(message);
        gameService.changeGameSetting(roomId, principal, message.getPayload().getMessage());
    }

    @MessageMapping("/room/round/{roomId}")
    public void roundChange(
            @DestinationVariable Long roomId,
            Message<DefaultWebSocketRequest<RoundChangeRequest>> message) {
        UserPrincipal principal = getSessionUser(message);
        gameService.changeGameSetting(roomId, principal, message.getPayload().getMessage());
    }
}
