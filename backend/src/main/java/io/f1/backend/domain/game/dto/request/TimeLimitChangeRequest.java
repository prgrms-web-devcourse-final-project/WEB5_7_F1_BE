package io.f1.backend.domain.game.dto.request;

import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.game.websocket.MessageSender;
import io.f1.backend.domain.quiz.app.QuizService;
import org.springframework.context.ApplicationEventPublisher;

public record TimeLimitChangeRequest(int timeLimit) implements GameSettingChanger {

    @Override
    public boolean change(Room room, QuizService quizService) {
        if (room.getTimeLimit() == timeLimit) {
            return false; // 동일하면 무시
        }
        room.changeTimeLimit(TimeLimit.from(timeLimit));
        return true;
    }

}
