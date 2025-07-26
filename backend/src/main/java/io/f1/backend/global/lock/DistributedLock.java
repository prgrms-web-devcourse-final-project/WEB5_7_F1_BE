package io.f1.backend.global.lock;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {

    String prefix();

    String key();

    // 시간단위를 초로 변경
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    // 락 점유를 위한 대기 시간
    long waitTime() default 5L;

    // 락 점유 시간
    long leaseTime() default 3L;
}
