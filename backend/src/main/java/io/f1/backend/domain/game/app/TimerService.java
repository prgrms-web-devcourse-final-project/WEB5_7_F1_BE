package io.f1.backend.domain.game.app;

import static io.f1.backend.domain.game.mapper.RoomMapper.ofPlayerEvent;
import static io.f1.backend.domain.game.mapper.RoomMapper.toQuestionResultResponse;
import static io.f1.backend.domain.game.mapper.RoomMapper.toQuestionStartResponse;

import io.f1.backend.domain.game.dto.MessageType;
import io.f1.backend.domain.game.dto.RoomEventType;
import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.game.websocket.MessageSender;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TimerService {

    private final MessageSender messageSender;

    private static final String NONE_CORRECT_USER = "";
    private static final int CONTINUE_DELAY = 3;

    public void startTimer(Room room, int delaySec) {
        cancelTimer(room);

        ScheduledFuture<?> timer = room.getScheduler().schedule(() -> {
            handleTimeout(room);
        }, delaySec + room.getGameSetting().getTimeLimit(), TimeUnit.SECONDS);

        room.updateTimer(timer);
    }

    private void handleTimeout(Room room) {
        String destination = getDestination(room.getId());

        messageSender.send(
                destination,
                MessageType.QUESTION_RESULT,
                toQuestionResultResponse(NONE_CORRECT_USER, room.getCurrentQuestion().getAnswer()));
        messageSender.send(
                destination,
                MessageType.SYSTEM_NOTICE,
                ofPlayerEvent(NONE_CORRECT_USER, RoomEventType.TIMEOUT));

        // TODO : 게임 종료 로직
        if (!validateCurrentRound(room)) {
            // 게임 종료 로직
            // GAME_SETTING, PLAYER_LIST, GAME_RESULT, ROOM_SETTING
            return;
        }

        // 다음 문제 출제
        room.increaseCurrentRound();

        startTimer(room, CONTINUE_DELAY);
        messageSender.send(
                destination,
                MessageType.QUESTION_START,
                toQuestionStartResponse(room, CONTINUE_DELAY));
    }

    public boolean validateCurrentRound(Room room) {
        if (room.getGameSetting().getRound() != room.getCurrentRound()) {
            return true;
        }
        cancelTimer(room);
        room.getScheduler().shutdown();
        return false;
    }

    public void cancelTimer(Room room) {
        // 정답 맞혔어요 ~ 타이머 캔슬 부탁
        ScheduledFuture<?> timer = room.getTimer();
        if (timer != null && !timer.isDone()) {
            timer.cancel(false);
        }
    }

    private String getDestination(Long roomId) {
        return "/sub/room/" + roomId;
    }
}
