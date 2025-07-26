package io.f1.backend.global.lock;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.f1.backend.global.config.RedissonTestContainerConfig;
import io.f1.backend.global.exception.CustomException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@Import({RedissonTestContainerConfig.class, DistributedLockIntegrationTest.TestLockService.class})
class DistributedLockIntegrationTest {

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.data.redis.host", RedissonTestContainerConfig.redisContainer::getHost);
        registry.add("spring.datasource.data.redis.port", () -> RedissonTestContainerConfig.redisContainer.getFirstMappedPort());
    }

    @Autowired
    private TestLockService testLockService;

    private final Long ROOM_ID = 1L;

    @DisplayName("멀티스레드 환경에서 하나의 스레드만 락 획득에 성공하고, 나머지는 모두 실패하는지 검증")
    @Test
    void testDistributedLock_WhenMultipleThreads_OnlyOneSuccess() throws Exception {
        // Given: 5개의 쓰레드로 구성된 고정된 쓰레드 풀과 동기화를 위한 CountDownLatch 준비
        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // When: 여러 스레드가 동시에 락 획득 시도
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    testLockService.executeWithLock(ROOM_ID);
                    successCount.incrementAndGet();
                } catch (IllegalStateException e) {
                    // 락 획득 실패로 인한 예외는 예상된 동작
                    failCount.incrementAndGet();
                } catch (Exception e) {
                    // 기타 예외는 실패로 간주
                    failCount.incrementAndGet();
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // Then: 하나의 스레드만 락 획득에 성공하고, 나머지는 모두 실패하는지 검증
        assertAll(
            () -> assertEquals(1, successCount.get(), "락 획득에 성공한 스레드는 1개여야 합니다"),
            () -> assertEquals(threadCount - 1, failCount.get(), "락 획득에 실패한 스레드는 " + (threadCount - 1) + "개여야 합니다")
        );
    }

    @DisplayName("단일 스레드에서 락 획득이 정상적으로 동작하는지 검증")
    @Test
    void testDistributedLock_SingleThread_Success() {
        // Given & When & Then
        String result = testLockService.executeWithLock(ROOM_ID);
        assertEquals("락 획득 및 실행 성공 : " + ROOM_ID, result);
    }

    @DisplayName("다른 키로 락을 사용할 때 동시 실행이 가능한지 검증")
    @Test
    void testDistributedLock_DifferentKeys_BothSuccess() throws Exception {
        // Given
        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // When: 서로 다른 키로 락 획득 시도
        executorService.submit(() -> {
            try {
                testLockService.executeWithLock(1L);
                successCount.incrementAndGet();
            } catch (CustomException e) {
                failCount.incrementAndGet();
                e.getMessage();
            } finally {
                latch.countDown();
            }
        });

        executorService.submit(() -> {
            try {
                testLockService.executeWithLock(2L);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
                e.getMessage();
            } finally {
                latch.countDown();
            }
        });

        latch.await();
        executorService.shutdown();

        // Then: 서로 다른 키이므로 둘 다 성공해야 함
        assertAll(
            () -> assertEquals(2, successCount.get(), "서로 다른 키로 락을 사용하면 둘 다 성공해야 한다"),
            () -> assertEquals(0, failCount.get(), "락 획득을 실패한 스레드는 없어야 한다")
        );
    }

    @DisplayName("gameStart 중에는 handlePlayerReady가 동일한 roomId로 락을 획득할 수 없어야 한다")
    @Test
    void testHandlePlayerReadyFailsWhenGameStartHoldsLock() throws Exception {
        // Given
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        CountDownLatch gameStartLocked = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        AtomicReference<String> successMethod = new AtomicReference<>();

        // Thread A: gameStart 락 선점
        executorService.submit(() -> {
            try {
                testLockService.gameStartSimulate(ROOM_ID, gameStartLocked);
                successMethod.set("gameStart");
                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
                e.getMessage();
            } finally {
                latch.countDown();
            }
        });

        // Thread A: gameStart가 락 획득한 후에 handlePlayerReady 시도
        executorService.submit(() -> {
            try {
                gameStartLocked.await();
                testLockService.handlePlayerReadySimulate(ROOM_ID, gameStartLocked);
                successMethod.set("handlePlayerReady");
                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
                e.getMessage();
            } finally {
                latch.countDown();
            }
        });

        latch.await();
        executorService.shutdown();

        // Then
        assertAll(
            () -> assertEquals(1, successCount.get(), "성공한 스레드는 1개여야 한다."),
            () -> assertEquals(1, failCount.get(), "실패한 스레드는 1개여야 한다."),
            () -> assertEquals("gameStart", successMethod.get(), "gameStart 락을 획득해야 한다")
        );
    }

    @DisplayName("handlePlayerReady 중에는 gameStart가 동일한 roomId로 락을 획득할 수 없어야 한다")
    @Test
    void testGameStartFailsWhenHandlePlayerReadyHoldsLock() throws Exception {
        // Given
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        CountDownLatch handlePlayerReadyLocked = new CountDownLatch(1);  // handlePlayerReady가 락 획득 신호용

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        AtomicReference<String> successMethod = new AtomicReference<>();

        // Thread B: handlePlayerReady가 락 먼저 획득
        executorService.submit(() -> {
            try {
                testLockService.handlePlayerReadySimulate(ROOM_ID, handlePlayerReadyLocked);
                successMethod.set("handlePlayerReady");
                successCount.incrementAndGet();
            } catch (CustomException e) {
                failCount.incrementAndGet();
                e.getMessage();
            } finally {
                handlePlayerReadyLocked.countDown(); // 락 획득 알림
                latch.countDown();
            }
        });

        // Thread A: handlePlayerReady가 락 획득한 후에 gameStart 시도
        executorService.submit(() -> {
            try {
                handlePlayerReadyLocked.await(); // handlePlayerReady 락 획득 신호 기다림
                testLockService.gameStartSimulate(ROOM_ID, handlePlayerReadyLocked);
                successMethod.set("gameStart");
                successCount.incrementAndGet();
            } catch (CustomException e) {
                failCount.incrementAndGet();
                e.getMessage();
            } catch (Exception e) {
                failCount.incrementAndGet();

            } finally {
                latch.countDown();
            }
        });

        latch.await();
        executorService.shutdown();

        // Then
        assertAll(
            () -> assertEquals(1, successCount.get(), "성공한 스레드는 1개여야 한다."),
            () -> assertEquals(1, failCount.get(), "실패한 스레드는 1개여야 한다."),
            () -> assertEquals("handlePlayerReady", successMethod.get(), "handlePlayerReady만 락을 획득해야 한다")
        );
    }

    /**
     * 테스트용 서비스 클래스
     */
    @Service
    static class TestLockService {

        @DistributedLock(prefix = "room", key = "#roomId", waitTime = 0)
        public String executeWithLock(Long roomId) {
            // 락이 획득된 상태에서 실행되는 비즈니스 로직 시뮬레이션
            try {
                Thread.sleep(100); // 짧은 작업 시간 시뮬레이션
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("작업 중단", e);
            }
            return "락 획득 및 실행 성공 : " + roomId;
        }

        @DistributedLock(prefix = "room", key = "#roomId", waitTime = 0)
        public String gameStartSimulate(Long roomId, CountDownLatch lockAcquiredSignal) {
            String threadName = Thread.currentThread().getName();
            lockAcquiredSignal.countDown();
            try {
                Thread.sleep(300); // 락 점유 시간 시뮬레이션
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "gameStart 실행 스레드 : " + threadName;
        }

        @DistributedLock(prefix = "room", key = "#roomId", waitTime = 0)
        public String handlePlayerReadySimulate(Long roomId, CountDownLatch lockAcquiredSignal) {
            String threadName = Thread.currentThread().getName();
            lockAcquiredSignal.countDown();
            try {
                Thread.sleep(300); // 락 점유 시간 시뮬레이션
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "handlePlayerReady 실행 스레드 : " + threadName;
        }
    }
}
