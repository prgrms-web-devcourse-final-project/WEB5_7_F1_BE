package io.f1.backend.domain.game.websocket;

import static io.f1.backend.domain.game.websocket.WebSocketUtils.getSessionId;
import static io.f1.backend.domain.game.websocket.WebSocketUtils.getSessionUser;

import io.f1.backend.domain.game.app.GameService;
import io.f1.backend.domain.game.app.RoomService;
import io.f1.backend.domain.game.dto.ChatMessage;
import io.f1.backend.domain.game.dto.request.DefaultWebSocketRequest;
import io.f1.backend.domain.user.dto.UserPrincipal;

import lombok.RequiredArgsConstructor;

import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class GameSocketController {

    // todo 삭제
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
    public void gameStart(@DestinationVariable Long roomId, Message<?> message) {

        UserPrincipal principal = getSessionUser(message);

        gameService.gameStart(roomId, principal);
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

    // todo 삭제
    private String getDestination(Long roomId) {
        return "/sub/room/" + roomId;
    }
}
