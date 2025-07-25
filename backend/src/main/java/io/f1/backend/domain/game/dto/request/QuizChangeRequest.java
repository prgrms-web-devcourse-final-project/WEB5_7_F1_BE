package io.f1.backend.domain.game.dto.request;

import static io.f1.backend.domain.game.mapper.RoomMapper.toPlayerListResponse;
import static io.f1.backend.domain.game.websocket.WebSocketUtils.getDestination;

import io.f1.backend.domain.game.dto.MessageType;
import io.f1.backend.domain.game.dto.response.PlayerListResponse;
import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.game.websocket.MessageSender;
import io.f1.backend.domain.quiz.app.QuizService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public record QuizChangeRequest(long quizId) implements GameSettingChanger {

    @Override
    public boolean change(Room room, QuizService quizService) {
        if (room.getQuizId() == quizId) {
            return false; // 동일하면 무시
        }
        Long questionsCount = quizService.getQuestionsCount(quizId);
        room.changeQuiz(quizId, questionsCount.intValue());
        return true;
    }

    @Override
    public void afterChange(Room room, MessageSender messageSender) {
        room.resetAllPlayerReadyStates();

        String destination = getDestination(room.getId());
        PlayerListResponse response = toPlayerListResponse(room);

        log.info(response.toString());
        messageSender.send(destination, MessageType.PLAYER_LIST, response);
    }
}
