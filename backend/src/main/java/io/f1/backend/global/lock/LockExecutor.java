package io.f1.backend.global.lock;

import static io.f1.backend.global.lock.DistributedLockAspect.LOCK_KEY_FORMAT;

import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.exception.errorcode.CommonErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class LockExecutor {

    private final RedissonClient redissonClient;

    // 시간단위를 초로 변경
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;

    // 락 점유를 위한 대기 시간
    private static final long DEFAULT_WAIT_TIME = 5L;

    // 락 점유 시간
    private static final long DEFAULT_LEASE_TIME = 3L;

    public <T> T executeWithLock(String prefix, Object key, Supplier<T> supplier) {
        String lockKey = formatLockKey(prefix, key);
        RLock rlock = redissonClient.getLock(lockKey);

        boolean acquired = false;
        try {
            acquired = rlock.tryLock(DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, DEFAULT_TIME_UNIT);

            if (!acquired) {
                log.warn("[LockExecutor] Lock acquisition failed: {}", key);
                throw new CustomException(CommonErrorCode.LOCK_ACQUISITION_FAILED);
            }
            log.info("[LockExecutor] Lock acquired: {}", key);

            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(CommonErrorCode.LOCK_INTERRUPTED);
        } finally {
            if (acquired && rlock.isHeldByCurrentThread()) {
                rlock.unlock();
                log.info("[LockExecutor] Lock released: {}", key);
            }
        }
    }

    public void executeWithLock(String prefix, Object key, Runnable runnable) {
        executeWithLock(
                prefix,
                key,
                () -> {
                    runnable.run();
                    return null;
                });
    }

    private String formatLockKey(String prefix, Object value) {
        return String.format(LOCK_KEY_FORMAT, prefix, value);
    }
}
