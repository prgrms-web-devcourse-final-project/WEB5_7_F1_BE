package io.f1.backend.domain.game.websocket;

import io.f1.backend.domain.game.app.RoomService;
import io.f1.backend.domain.game.dto.MessageType;
import io.f1.backend.domain.game.dto.RoomExitData;
import io.f1.backend.domain.game.dto.RoomInitialData;

import io.f1.backend.domain.game.dto.request.GameStartRequest;
import io.f1.backend.domain.game.dto.response.GameStartResponse;
import io.f1.backend.domain.quiz.app.QuizService;
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
    private final QuizService quizService;

    @MessageMapping("/room/enter/{roomId}")
    public void roomEnter(@DestinationVariable Long roomId, Message<?> message) {

        String websocketSessionId = getSessionId(message);

        RoomInitialData roomInitialData = roomService.enterRoom(roomId, websocketSessionId);
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

        Integer round = roomService.checkGameSetting(roomId, quizId);

        // TODO : 라운드 수만큼 랜덤하게 문제 주기..!
        GameStartResponse questions = quizService.getQuestionsWithoutAnswer(quizId, round);

        roomService.gameStart(roomId);

    }

    private static String getSessionId(Message<?> message) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        return accessor.getSessionId();
    }
}
