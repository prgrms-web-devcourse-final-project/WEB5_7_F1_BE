package io.f1.backend.domain.game.websocket;

import io.f1.backend.domain.game.app.GameService;
import io.f1.backend.domain.game.app.RoomService;
import io.f1.backend.domain.game.dto.ChatMessage;
import io.f1.backend.domain.game.dto.MessageType;
import io.f1.backend.domain.game.dto.request.DefaultWebSocketRequest;
import io.f1.backend.domain.game.dto.response.GameStartResponse;
import io.f1.backend.domain.user.dto.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class GameSocketController {

    //todo 삭제
    private final MessageSender messageSender;
    private final RoomService roomService;
    private final GameService gameService;

    @MessageMapping("/room/initializeRoomSocket/{roomId}")
    public void initializeRoomSocket(@DestinationVariable Long roomId, Message<?> message) {

        String websocketSessionId = getSessionId(message);

        UserPrincipal principal = getSessionUser(message);

        roomService.initializeRoomSocket(roomId, websocketSessionId, principal);
    }

    @MessageMapping("/room/exit/{roomId}")
    public void exitRoom(@DestinationVariable Long roomId, Message<?> message) {

        String websocketSessionId = getSessionId(message);
        UserPrincipal principal = getSessionUser(message);

        roomService.exitRoom(roomId, websocketSessionId, principal);
    }

    @MessageMapping("/room/start/{roomId}")
    public void gameStart(
        @DestinationVariable Long roomId,
        Message<DefaultWebSocketRequest<GameStartRequest>> message) {
    public void gameStart(@DestinationVariable Long roomId, Message<?> message) {

        GameStartResponse gameStartResponse =
            gameService.gameStart(roomId, message.getPayload().getMessage());
        UserPrincipal principal = getSessionUser(message);

        GameStartResponse gameStartResponse = gameService.gameStart(roomId, principal);

        String destination = getDestination(roomId);

        messageSender.send(destination, MessageType.GAME_START, gameStartResponse);
    }

    @MessageMapping("room/chat/{roomId}")
    public void chat(
        @DestinationVariable Long roomId,
        Message<DefaultWebSocketRequest<ChatMessage>> message) {

        roomService.chat(roomId, getSessionId(message), message.getPayload().getMessage());
    }

    @MessageMapping("/room/ready/{roomId}")
    public void playerReady(@DestinationVariable Long roomId, Message<?> message) {

        roomService.handlePlayerReady(roomId, getSessionId(message));
    }

    private static String getSessionId(Message<?> message) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        return accessor.getSessionId();
    }

    private static UserPrincipal getSessionUser(Message<?> message) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        Authentication auth = (Authentication) accessor.getUser();
        return (UserPrincipal) auth.getPrincipal();
    }

    //todo 삭제
    private String getDestination(Long roomId) {
        return "/sub/room/" + roomId;
    }
}
