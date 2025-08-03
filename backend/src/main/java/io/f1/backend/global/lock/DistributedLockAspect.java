package io.f1.backend.global.lock;

import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.exception.errorcode.CommonErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAspect {

    public static final String LOCK_KEY_FORMAT = "lock:%s:{%s}";

    private final RedissonClient redissonClient;

    @Around("@annotation(distributedLock)")
    public Object lock(ProceedingJoinPoint joinPoint, DistributedLock distributedLock)
            throws Throwable {

        String key = getLockKey(joinPoint, distributedLock);

        RLock rlock = redissonClient.getLock(key);

        boolean acquired = false;
        try {
            acquired =
                    rlock.tryLock(
                            distributedLock.waitTime(),
                            distributedLock.leaseTime(),
                            distributedLock.timeUnit());

            if (!acquired) {
                log.warn("[DistributedLock] Lock acquisition failed: {}", key);
                throw new CustomException(CommonErrorCode.LOCK_ACQUISITION_FAILED);
            }
            log.info("[DistributedLock] Lock acquired: {}", key);

            return joinPoint.proceed();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } finally {
            if (acquired && rlock.isHeldByCurrentThread()) {
                rlock.unlock();
                log.info("[DistributedLock] Lock released: {}", key);
            }
        }
    }

    private String getLockKey(ProceedingJoinPoint joinPoint, DistributedLock lockAnnotation) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        String keyExpr = lockAnnotation.key();
        String prefix = lockAnnotation.prefix();

        Object keyValueObj =
                CustomSpringELParser.getDynamicValue(
                        signature.getParameterNames(), joinPoint.getArgs(), keyExpr);
        String keyValue = String.valueOf(keyValueObj);

        return String.format(LOCK_KEY_FORMAT, prefix, keyValue);
    }
}
