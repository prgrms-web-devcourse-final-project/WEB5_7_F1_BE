package io.f1.backend.domain.game.dto.request;

import static io.f1.backend.domain.game.mapper.RoomMapper.toPlayerListResponse;

import io.f1.backend.domain.game.dto.MessageType;
import io.f1.backend.domain.game.dto.response.PlayerListResponse;
import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.game.websocket.MessageSender;
import io.f1.backend.domain.quiz.app.QuizService;
import io.f1.backend.domain.quiz.entity.Quiz;

import java.util.Objects;

public record QuizChangeRequest(Long quizId) implements GameSettingChanger {

    @Override
    public boolean change(Room room, QuizService quizService) {
        if (Objects.equals(room.getGameSetting().getQuizId(), quizId)) {
            return false; // 동일하면 무시
        }
        Quiz quiz = quizService.getQuizWithQuestionsById(quizId);
        int questionSize = quiz.getQuestions().size();
        room.getGameSetting().changeQuiz(quiz);
        // 퀴즈의 문제 갯수로 변경
        room.getGameSetting().changeRound(questionSize, questionSize);
        return true;
    }

    @Override
    public void afterChange(Room room, MessageSender messageSender) {
        room.resetAllPlayerReadyStates();

        String destination = "/sub/room/" + room.getId();
        PlayerListResponse response = toPlayerListResponse(room);

        messageSender.send(destination, MessageType.PLAYER_LIST, response);
    }
}
