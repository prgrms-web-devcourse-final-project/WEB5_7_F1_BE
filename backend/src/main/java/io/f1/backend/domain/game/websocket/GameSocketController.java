package io.f1.backend.domain.game.websocket;

import io.f1.backend.domain.game.app.RoomService;
import io.f1.backend.domain.game.dto.MessageType;
import io.f1.backend.domain.game.dto.RoomInitialData;

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

    @MessageMapping("/room/enter/{roomId}")
    public void roomEnter(@DestinationVariable Long roomId, Message<?> message) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        String websocketSessionId = accessor.getSessionId();

        RoomInitialData roomInitialData = roomService.enterRoom(roomId, websocketSessionId);
        String destination = roomInitialData.destination();

        messageSender.send(
                destination, MessageType.ROOM_SETTING, roomInitialData.roomSettingResponse());
        messageSender.send(
                destination, MessageType.GAME_SETTING, roomInitialData.gameSettingResponse());
        messageSender.send(
                destination, MessageType.PLAYER_LIST, roomInitialData.playerListResponse());
    }
}
