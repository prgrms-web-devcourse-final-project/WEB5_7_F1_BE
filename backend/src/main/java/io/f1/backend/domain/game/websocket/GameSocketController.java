package io.f1.backend.domain.game.websocket;

import io.f1.backend.domain.game.app.GameService;
import io.f1.backend.domain.game.app.RoomService;
import io.f1.backend.domain.game.dto.GameStartData;
import io.f1.backend.domain.game.dto.MessageType;
import io.f1.backend.domain.game.dto.PlayerReadyData;
import io.f1.backend.domain.game.dto.RoomExitData;
import io.f1.backend.domain.game.dto.RoomInitialData;
import io.f1.backend.domain.game.dto.request.GameStartRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class GameSocketController {

    private final MessageSender messageSender;
    private final RoomService roomService;
    private final GameService gameService;

    @MessageMapping("/room/initializeRoomSocket/{roomId}")
    public void initializeRoomSocket(@DestinationVariable Long roomId, Message<?> message) {

        String websocketSessionId = getSessionId(message);

        RoomInitialData roomInitialData =
                roomService.initializeRoomSocket(roomId, websocketSessionId);

        String destination = roomInitialData.destination();

        messageSender.send(
                destination, MessageType.ROOM_SETTING, roomInitialData.roomSettingResponse());
        messageSender.send(
                destination, MessageType.GAME_SETTING, roomInitialData.gameSettingResponse());
        messageSender.send(
                destination, MessageType.PLAYER_LIST, roomInitialData.playerListResponse());
        messageSender.send(
                destination, MessageType.SYSTEM_NOTICE, roomInitialData.systemNoticeResponse());
    }

    @MessageMapping("/room/exit/{roomId}")
    public void exitRoom(@DestinationVariable Long roomId, Message<?> message) {

        String websocketSessionId = getSessionId(message);

        RoomExitData roomExitData = roomService.exitRoom(roomId, websocketSessionId);

        String destination = roomExitData.getDestination();

        if (!roomExitData.isRemovedRoom()) {
            messageSender.send(
                    destination, MessageType.PLAYER_LIST, roomExitData.getPlayerListResponses());
            messageSender.send(
                    destination, MessageType.SYSTEM_NOTICE, roomExitData.getSystemNoticeResponse());
        }
    }

    @MessageMapping("/room/start/{roomId}")
    public void gameStart(@DestinationVariable Long roomId, Message<GameStartRequest> message) {

        Long quizId = message.getPayload().quizId();

        GameStartData gameStartData = gameService.gameStart(roomId, quizId);

        String destination = gameStartData.destination();

        messageSender.send(destination, MessageType.GAME_START, gameStartData.gameStartResponse());
    }

    @MessageMapping("/room/ready/{roomId}")
    public void playerReady(@DestinationVariable Long roomId, Message<?> message) {

        PlayerReadyData playerReadyData =
                roomService.handlePlayerReady(roomId, getSessionId(message));

        messageSender.send(
                playerReadyData.destination(), MessageType.PLAYER_LIST, playerReadyData.response());
    }

    private static String getSessionId(Message<?> message) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        return accessor.getSessionId();
    }
}
