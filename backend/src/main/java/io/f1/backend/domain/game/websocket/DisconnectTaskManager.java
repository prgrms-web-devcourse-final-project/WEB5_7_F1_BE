package io.f1.backend.domain.game.websocket;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class DisconnectTaskManager {

    // todo 부하테스트 후 스레드 풀 변경
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final Map<Long, ScheduledFuture<?>> disconnectTasks = new ConcurrentHashMap<>();

    public void scheduleDisconnectTask(Long userId, Runnable task) {

        /* 5초 뒤 실행 */
        ScheduledFuture<?> scheduled = scheduler.schedule(task, 5, TimeUnit.SECONDS);

        ScheduledFuture<?> prev = disconnectTasks.put(userId, scheduled);
        cancelIfRunning(prev);
    }

    public void cancelDisconnectTask(Long userId) {
        ScheduledFuture<?> task = disconnectTasks.remove(userId);
        cancelIfRunning(task);
    }

    private void cancelIfRunning(ScheduledFuture<?> future) {
        if (future != null && !future.isDone()) {
            future.cancel(false);
        }
    }
}
