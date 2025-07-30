package io.f1.backend.domain.game.app;

import static io.f1.backend.domain.game.websocket.WebSocketUtils.getDestination;

import io.f1.backend.domain.game.dto.ChatMessage;
import io.f1.backend.domain.game.dto.MessageType;
import io.f1.backend.domain.game.event.GameCorrectAnswerEvent;
import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.game.websocket.MessageSender;
import io.f1.backend.domain.question.entity.Question;
import io.f1.backend.domain.user.dto.UserPrincipal;

import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final RoomService roomService;
    private final MessageSender messageSender;
    private final ApplicationEventPublisher eventPublisher;

    // todo 동시성적용
    public void chat(Long roomId, UserPrincipal userPrincipal, ChatMessage chatMessage) {

        Room room = roomService.findRoom(roomId);

        String destination = getDestination(roomId);

        messageSender.sendBroadcast(destination, MessageType.CHAT, chatMessage);

        if (!room.isPlaying()) {
            return;
        }

        Question currentQuestion = room.getCurrentQuestion();

        String answer = currentQuestion.getAnswer();

        if (!answer.equals(chatMessage.message())) {
            return;
        }

        // false -> true
        if (room.compareAndSetAnsweredFlag(false, true)) {
            eventPublisher.publishEvent(
                    new GameCorrectAnswerEvent(
                            room, userPrincipal.getUserId(), chatMessage, answer));
        }
    }
}
