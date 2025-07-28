package io.f1.backend.domain.game.app;

import io.f1.backend.domain.game.event.GameTimeoutEvent;
import io.f1.backend.domain.game.model.Room;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimerService {

    private final ApplicationEventPublisher eventPublisher;

    public void startTimer(Room room, int delaySec) {
        log.debug("Starting timer : round " + room.getCurrentRound());
        cancelTimer(room);

        ScheduledFuture<?> timer =
                room.getScheduler()
                        .schedule(
                                () -> {
                                    eventPublisher.publishEvent(new GameTimeoutEvent(room));
                                },
                                delaySec + room.getGameSetting().getTimeLimit(),
                                TimeUnit.SECONDS);

        room.updateTimer(timer);
    }

    public boolean validateCurrentRound(Room room) {
        if (room.getGameSetting().getRound() != room.getCurrentRound()) {
            return true;
        }
        cancelTimer(room);
        room.getScheduler().shutdown();
        return false;
    }

    public boolean cancelTimer(Room room) {
        // 정답 맞혔어요 ~ 타이머 캔슬 부탁
        log.debug("Canceling timer : round " + room.getCurrentRound());
        ScheduledFuture<?> timer = room.getTimer();
        if (timer != null && !timer.isDone()) {
            return timer.cancel(false);
        }
        return false;
    }
}
