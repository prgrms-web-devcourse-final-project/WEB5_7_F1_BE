package io.f1.backend.global.lock;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.f1.backend.global.exception.CustomException;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
class DistributedLockAspectTests {

    @InjectMocks DistributedLockAspect distributedLockAspect;

    @Mock RedissonClient redissonClient;

    @Mock RLock rLock;

    @Mock ProceedingJoinPoint joinPoint;

    @Mock MethodSignature methodSignature;

    @Mock DistributedLock distributedLockAnnotation;

    private final String TEST_PREFIX = "room";
    private final String TEST_KEY = "#roomId";
    private final String TEST_ROOM_ID = "12345";
    private final long WAIT_TIME = 5L;
    private final long LEASE_TIME = 3L;
    private final String EXPECTED_LOCK_KEY = "lock:room:{12345}";
    private final Object EXPECTED_RETURN_VALUE = "success";

    @BeforeEach
    void setUp() {
        // 모든 테스트에서 공통으로 사용되는 기본 설정만 유지
        when(distributedLockAnnotation.prefix()).thenReturn(TEST_PREFIX);
        when(distributedLockAnnotation.waitTime()).thenReturn(WAIT_TIME);
        when(distributedLockAnnotation.leaseTime()).thenReturn(LEASE_TIME);
        when(distributedLockAnnotation.timeUnit()).thenReturn(TimeUnit.SECONDS);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[] {"roomId"});
        when(joinPoint.getArgs()).thenReturn(new Object[] {TEST_ROOM_ID});
    }

    @DisplayName("락 획득 성공 시 정상적으로 메서드가 실행되고, 락 해제가 호출되는지 확인")
    @Test
    void testLock_Success() throws Throwable {
        // Given
        when(distributedLockAnnotation.key()).thenReturn(TEST_KEY);
        when(redissonClient.getLock(EXPECTED_LOCK_KEY)).thenReturn(rLock);
        when(rLock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS)).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        when(joinPoint.proceed()).thenReturn(EXPECTED_RETURN_VALUE);

        try (MockedStatic<CustomSpringELParser> mockedParser =
                Mockito.mockStatic(CustomSpringELParser.class)) {
            mockedParser
                    .when(
                            () ->
                                    CustomSpringELParser.getDynamicValue(
                                            any(String[].class), any(Object[].class), anyString()))
                    .thenReturn(TEST_ROOM_ID);

            // When
            Object result = distributedLockAspect.lock(joinPoint, distributedLockAnnotation);

            // Then
            assertAll(
                    () -> assertEquals(EXPECTED_RETURN_VALUE, result),
                    () -> verify(redissonClient, times(1)).getLock(EXPECTED_LOCK_KEY),
                    () -> verify(rLock, times(1)).tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS),
                    () -> verify(joinPoint, times(1)).proceed(),
                    () -> verify(rLock, times(1)).isHeldByCurrentThread(),
                    () -> verify(rLock, times(1)).unlock());
        }
    }

    @DisplayName("락 획득 실패 시 CustomException(LOCK_ACQUISITION_FAILED)이 발생하는지 확인")
    @Test
    void testLock_FailToAcquireLock() throws Throwable {
        // Given
        when(distributedLockAnnotation.key()).thenReturn(TEST_KEY);
        when(redissonClient.getLock(EXPECTED_LOCK_KEY)).thenReturn(rLock);
        // 무조건 false 반환 하도록 강제
        when(rLock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS)).thenReturn(false);

        try (MockedStatic<CustomSpringELParser> mockedParser =
                Mockito.mockStatic(CustomSpringELParser.class)) {
            mockedParser
                    .when(
                            () ->
                                    CustomSpringELParser.getDynamicValue(
                                            any(String[].class), any(Object[].class), anyString()))
                    .thenReturn(TEST_ROOM_ID);

            // When & Then
            CustomException exception =
                    assertThrows(
                            CustomException.class,
                            () -> distributedLockAspect.lock(joinPoint, distributedLockAnnotation));

            assertAll(
                    () -> assertNotNull(exception),
                    () -> assertEquals("다른 요청이 작업 중입니다. 잠시 후 다시 시도해주세요.", exception.getMessage()),
                    () -> verify(redissonClient, times(1)).getLock(EXPECTED_LOCK_KEY),
                    () -> verify(rLock, times(1)).tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS),
                    () -> verify(joinPoint, never()).proceed(),
                    () -> verify(rLock, never()).unlock());
        }
    }

    @DisplayName("락 대기 중 인터럽트 발생 시 InterruptedException이 전파되는지 확인")
    @Test
    void testLock_InterruptedException() throws Throwable {
        // Given
        when(distributedLockAnnotation.key()).thenReturn(TEST_KEY);
        when(redissonClient.getLock(EXPECTED_LOCK_KEY)).thenReturn(rLock);
        when(rLock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS))
                .thenThrow(new InterruptedException("Thread interrupted"));

        try (MockedStatic<CustomSpringELParser> mockedParser =
                Mockito.mockStatic(CustomSpringELParser.class)) {
            mockedParser
                    .when(
                            () ->
                                    CustomSpringELParser.getDynamicValue(
                                            any(String[].class), any(Object[].class), anyString()))
                    .thenReturn(TEST_ROOM_ID);

            // When & Then
            InterruptedException exception =
                    assertThrows(
                            InterruptedException.class,
                            () -> distributedLockAspect.lock(joinPoint, distributedLockAnnotation));

            assertAll(
                    () -> assertNotNull(exception),
                    () -> assertEquals("Thread interrupted", exception.getMessage()),
                    () -> verify(redissonClient, times(1)).getLock(EXPECTED_LOCK_KEY),
                    () -> verify(rLock, times(1)).tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS),
                    () -> verify(joinPoint, never()).proceed(),
                    () -> verify(rLock, never()).unlock());
        }
    }

    @DisplayName("락을 획득하지 않은 스레드가 unlock하지 않는지 확인")
    @Test
    void testLock_NotHeldByCurrentThread() throws Throwable {
        // Given
        when(distributedLockAnnotation.key()).thenReturn(TEST_KEY);
        when(redissonClient.getLock(EXPECTED_LOCK_KEY)).thenReturn(rLock);
        when(rLock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS)).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(false); // 현재 스레드가 락을 보유하지 않도록 강제
        when(joinPoint.proceed()).thenReturn(EXPECTED_RETURN_VALUE);

        try (MockedStatic<CustomSpringELParser> mockedParser =
                Mockito.mockStatic(CustomSpringELParser.class)) {
            mockedParser
                    .when(
                            () ->
                                    CustomSpringELParser.getDynamicValue(
                                            any(String[].class), any(Object[].class), anyString()))
                    .thenReturn(TEST_ROOM_ID);

            // When
            Object result = distributedLockAspect.lock(joinPoint, distributedLockAnnotation);

            // Then
            assertAll(
                    () -> assertEquals(EXPECTED_RETURN_VALUE, result),
                    () -> verify(redissonClient, times(1)).getLock(EXPECTED_LOCK_KEY),
                    () -> verify(rLock, times(1)).tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS),
                    () -> verify(joinPoint, times(1)).proceed(),
                    () -> verify(rLock, times(1)).isHeldByCurrentThread(),
                    () -> verify(rLock, never()).unlock() // unlock 호출되지 않아야 함
                    );
        }
    }

    @DisplayName("메서드 실행 중 예외 발생 시에도 락이 정상적으로 해제되는지 확인")
    @Test
    void testLock_ExceptionDuringExecution() throws Throwable {
        // Given
        RuntimeException testException = new RuntimeException("Test exception");
        when(distributedLockAnnotation.key()).thenReturn(TEST_KEY);
        when(redissonClient.getLock(EXPECTED_LOCK_KEY)).thenReturn(rLock);
        when(rLock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS)).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        when(joinPoint.proceed()).thenThrow(testException);

        try (MockedStatic<CustomSpringELParser> mockedParser =
                Mockito.mockStatic(CustomSpringELParser.class)) {
            mockedParser
                    .when(
                            () ->
                                    CustomSpringELParser.getDynamicValue(
                                            any(String[].class), any(Object[].class), anyString()))
                    .thenReturn(TEST_ROOM_ID);

            // When & Then
            RuntimeException exception =
                    assertThrows(
                            RuntimeException.class,
                            () -> distributedLockAspect.lock(joinPoint, distributedLockAnnotation));

            assertAll(
                    () -> assertEquals(testException, exception),
                    () -> verify(redissonClient, times(1)).getLock(EXPECTED_LOCK_KEY),
                    () -> verify(rLock, times(1)).tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS),
                    () -> verify(joinPoint, times(1)).proceed(),
                    () -> verify(rLock, times(1)).isHeldByCurrentThread(),
                    () -> verify(rLock, times(1)).unlock() // 예외 발생해도 unlock 호출되어야 함
                    );
        }
    }

    @DisplayName("SpEL 파싱을 통한 동적 키 생성이 정상적으로 작동하는지 확인")
    @Test
    void testGetLockKey_WithSpELExpression() throws Throwable {
        // Given
        String roomId = "room123";
        String expectedLockKey = "lock:room:{room123}";

        when(distributedLockAnnotation.prefix()).thenReturn("room");
        when(distributedLockAnnotation.key()).thenReturn("#roomId");
        when(methodSignature.getParameterNames()).thenReturn(new String[] {"roomId"});
        when(joinPoint.getArgs()).thenReturn(new Object[] {roomId});

        when(redissonClient.getLock(expectedLockKey)).thenReturn(rLock);
        when(rLock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS)).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        when(joinPoint.proceed()).thenReturn(EXPECTED_RETURN_VALUE);

        try (MockedStatic<CustomSpringELParser> mockedParser =
                Mockito.mockStatic(CustomSpringELParser.class)) {
            mockedParser
                    .when(
                            () ->
                                    CustomSpringELParser.getDynamicValue(
                                            new String[] {"roomId"},
                                            new Object[] {roomId},
                                            "#roomId"))
                    .thenReturn(roomId);

            // When
            Object result = distributedLockAspect.lock(joinPoint, distributedLockAnnotation);

            // Then
            assertAll(
                    () -> assertEquals(EXPECTED_RETURN_VALUE, result),
                    () -> verify(redissonClient, times(1)).getLock(expectedLockKey),
                    () -> verify(rLock, times(1)).unlock());
        }
    }
}
