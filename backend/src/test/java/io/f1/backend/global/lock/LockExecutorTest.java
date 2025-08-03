package io.f1.backend.global.lock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.exception.errorcode.CommonErrorCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@ExtendWith(MockitoExtension.class)
class LockExecutorTest {

    @Mock private RedissonClient redissonClient;

    @Mock private RLock rlock;

    @InjectMocks private LockExecutor lockExecutor;

    private final String TEST_PREFIX = "room";
    private final Long TEST_ROOM_ID = 1L;
    private final long WAIT_TIME = 5L;
    private final long LEASE_TIME = 3L;
    private final String EXPECTED_LOCK_KEY = "lock:" + TEST_PREFIX + ":{" + TEST_ROOM_ID + "}";
    private final String EXPECTED_RETURN_VALUE = "success";

    @Test
    @DisplayName("락 획득 성공 시 supplier 로직이 실행되고 락 해제됨")
    void executeWithLock_successfulLock_supplier() throws Exception {
        // given

        when(redissonClient.getLock(anyString())).thenReturn(rlock);
        when(rlock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS)).thenReturn(true);
        when(rlock.isHeldByCurrentThread()).thenReturn(true);

        // when
        String result =
                lockExecutor.executeWithLock(
                        TEST_PREFIX, TEST_ROOM_ID, () -> EXPECTED_RETURN_VALUE);

        // then
        assertEquals(EXPECTED_RETURN_VALUE, result);
        verify(redissonClient).getLock(EXPECTED_LOCK_KEY);
        verify(redissonClient, times(1)).getLock(EXPECTED_LOCK_KEY);
        verify(rlock).unlock();
    }

    @Test
    @DisplayName("락 획득 실패 시 CustomException(LOCK_ACQUISITION_FAILED)이 발생하는지 확인")
    void executeWithLock_failToAcquireLock() throws Exception {

        when(redissonClient.getLock(anyString())).thenReturn(rlock);
        when(rlock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS)).thenReturn(false);

        // when & then
        CustomException ex =
                assertThrows(
                        CustomException.class,
                        () ->
                                lockExecutor.executeWithLock(
                                        TEST_PREFIX, TEST_ROOM_ID, () -> "SHOULD_NOT_RUN"));

        assertEquals(CommonErrorCode.LOCK_ACQUISITION_FAILED, ex.getErrorCode());
        verify(redissonClient, times(1)).getLock(EXPECTED_LOCK_KEY);
        verify(rlock, times(1)).tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS);
        verify(rlock, never()).unlock();
    }

    @Test
    @DisplayName("InterruptedException 발생 시 CustomException 발생 및 인터럽트 설정")
    void executeWithLock_interruptedException() throws Exception {

        when(redissonClient.getLock(anyString())).thenReturn(rlock);
        when(rlock.tryLock(5L, 3L, TimeUnit.SECONDS)).thenThrow(new InterruptedException());

        // when & then
        CustomException ex =
                assertThrows(
                        CustomException.class,
                        () ->
                                lockExecutor.executeWithLock(
                                        TEST_PREFIX, TEST_ROOM_ID, () -> "SHOULD_NOT_RUN"));

        verify(redissonClient, times(1)).getLock(EXPECTED_LOCK_KEY);
        verify(rlock, times(1)).tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS);
        assertEquals(CommonErrorCode.LOCK_INTERRUPTED, ex.getErrorCode());
        assertTrue(Thread.currentThread().isInterrupted());
        verify(rlock, never()).unlock();
    }

    @Test
    @DisplayName("Runnable 버전 executeWithLock 정상 동작")
    void executeWithLock_runnableVersion() throws Exception {
        // given
        AtomicBoolean executed = new AtomicBoolean(false);

        when(redissonClient.getLock(anyString())).thenReturn(rlock);
        when(rlock.tryLock(5L, 3L, TimeUnit.SECONDS)).thenReturn(true);
        when(rlock.isHeldByCurrentThread()).thenReturn(true);

        // when
        lockExecutor.executeWithLock(TEST_PREFIX, TEST_ROOM_ID, () -> executed.set(true));

        // then
        verify(redissonClient, times(1)).getLock(EXPECTED_LOCK_KEY);
        verify(rlock, times(1)).tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS);
        assertTrue(executed.get());
        verify(rlock).unlock();
    }

    @Test
    @DisplayName("락을 획득했지만 현재 스레드가 소유하지 않은 경우 unlock 하지 않음")
    void executeWithLock_notHeldByCurrentThread_shouldNotUnlock() throws Exception {
        // given
        when(redissonClient.getLock(EXPECTED_LOCK_KEY)).thenReturn(rlock);
        when(rlock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS)).thenReturn(true);
        when(rlock.isHeldByCurrentThread()).thenReturn(false);

        // when
        String result = lockExecutor.executeWithLock(TEST_PREFIX, TEST_ROOM_ID, () -> "EXECUTED");

        // then
        verify(redissonClient, times(1)).getLock(EXPECTED_LOCK_KEY);
        verify(rlock, times(1)).tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS);
        assertEquals("EXECUTED", result);
        verify(rlock, never()).unlock();
    }

    @Test
    @DisplayName("락을 획득하지 않은 스레드가 unlock하지 않는지 확인")
    void executeWithLock_lockNotAcquired_shouldNotUnlock() throws Exception {
        // given
        when(redissonClient.getLock(EXPECTED_LOCK_KEY)).thenReturn(rlock);
        when(rlock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS)).thenReturn(false);

        // when & then
        CustomException ex =
                assertThrows(
                        CustomException.class,
                        () ->
                                lockExecutor.executeWithLock(
                                        TEST_PREFIX,
                                        TEST_ROOM_ID,
                                        () -> {
                                            throw new IllegalStateException(
                                                    "Should not be executed");
                                        }));

        verify(redissonClient, times(1)).getLock(EXPECTED_LOCK_KEY);
        verify(rlock, times(1)).tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS);
        assertEquals(CommonErrorCode.LOCK_ACQUISITION_FAILED, ex.getErrorCode());
        verify(rlock, never()).unlock();
    }

    @Test
    @DisplayName("메서드 실행 중 예외 발생 시에도 락이 정상적으로 해제되는지 확인")
    void executeWithLock_exceptionThrown_shouldUnlock() throws Exception {
        // given
        when(redissonClient.getLock(EXPECTED_LOCK_KEY)).thenReturn(rlock);
        when(rlock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS)).thenReturn(true);
        when(rlock.isHeldByCurrentThread()).thenReturn(true);

        // when & then
        RuntimeException ex =
                assertThrows(
                        RuntimeException.class,
                        () ->
                                lockExecutor.executeWithLock(
                                        TEST_PREFIX,
                                        TEST_ROOM_ID,
                                        () -> {
                                            throw new RuntimeException("exception");
                                        }));

        assertEquals("exception", ex.getMessage());

        verify(redissonClient, times(1)).getLock(EXPECTED_LOCK_KEY);
        verify(rlock, times(1)).tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS);
        verify(rlock).unlock();
    }
}
