package io.f1.backend.domain.game.app;

import static io.f1.backend.domain.game.mapper.RoomMapper.ofPlayerEvent;
import static io.f1.backend.domain.game.mapper.RoomMapper.toQuestionStartResponse;
import static io.f1.backend.domain.game.mapper.RoomMapper.toRankUpdateResponse;

import io.f1.backend.domain.game.dto.MessageType;
import io.f1.backend.domain.game.dto.RoomEventType;
import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.game.websocket.MessageSender;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TimerService {

    private final MessageSender messageSender;

    private static final int CONTINUE_DELAY = 3;

    public void startTimer(Room room, int delaySec) {
        ScheduledFuture<?> timer = room.getTimer();
        timer = room.getScheduler().schedule(() -> {
            handleTimeout(room);
        }, delaySec + room.getGameSetting().getTimeLimit(), TimeUnit.SECONDS);
    }

    private void handleTimeout(Room room) {
        // TIMEOUT 일 때 처리 !
        String destination = getDestination(room.getId());

        room.increaseCurrentRound();

        // QuestionResult는 ChatMessage 받지 않고 String 받도록
        messageSender.send(destination, MessageType.RANK_UPDATE, toRankUpdateResponse(room));
        messageSender.send(destination, MessageType.SYSTEM_NOTICE, ofPlayerEvent("", RoomEventType.TIMEOUT));
        startTimer(room, CONTINUE_DELAY);
        messageSender.send(destination, MessageType.QUESTION_START, toQuestionStartResponse(room.getCurrentQuestion().getId(), room.getCurrentRound()));

        // TIMEOUT 처리 해주고 또 타이머 시작 !
    }

    public void cancelTimer(Room room) {
        // 정답 맞혔어요 ~ 타이머 캔슬 부탁
        ScheduledFuture<?> timer = room.getTimer();
        if(timer != null) {
            timer.cancel(false);
        }
    }

    private String getDestination(Long roomId) {
        return "/sub/room/" + roomId;
    }
}
