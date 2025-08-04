package io.f1.backend.domain.game.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.f1.backend.domain.game.dto.request.RoomValidationRequest;
import io.f1.backend.domain.game.model.GameSetting;
import io.f1.backend.domain.game.model.Player;
import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.game.model.RoomSetting;
import io.f1.backend.domain.game.store.RoomRepository;
import io.f1.backend.domain.game.store.UserRoomRepository;
import io.f1.backend.domain.game.websocket.DisconnectTaskManager;
import io.f1.backend.domain.game.websocket.MessageSender;
import io.f1.backend.domain.quiz.app.QuizService;
import io.f1.backend.domain.quiz.dto.QuizMinData;
import io.f1.backend.domain.quiz.entity.Quiz;
import io.f1.backend.domain.user.dto.UserPrincipal;
import io.f1.backend.domain.user.entity.User;
import io.f1.backend.global.config.RedisTestContainerConfig;
import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.exception.errorcode.RoomErrorCode;
import io.f1.backend.global.lock.LockExecutor;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@SpringBootTest
@Import({RedisTestContainerConfig.class}) // Redis Testcontainers 설정 임포트
class RoomServiceConcurrentTest {

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", RedisTestContainerConfig.redisContainer::getHost);
        registry.add(
                "spring.data.redis.port",
                () -> RedisTestContainerConfig.redisContainer.getFirstMappedPort());
    }

    @Mock private QuizService quizService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private DisconnectTaskManager disconnectTasks;
    @Mock private MessageSender messageSender;

    // RoomRepository와 UserRoomRepository는 실제 Map 기반 구현체를 사용
    private RoomRepository roomRepository;
    private UserRoomRepository userRoomRepository;

    @Autowired private LockExecutor lockExecutor;

    @InjectMocks private RoomService roomService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // RoomRepository 및 UserRoomRepository의 인메모리 구현체 초기화
        this.roomRepository = new InMemoryRoomRepository();
        this.userRoomRepository = new UserRoomRepository();

        // RoomService에 실제 구현체 주입
        ReflectionTestUtils.setField(roomService, "roomRepository", roomRepository);
        ReflectionTestUtils.setField(roomService, "userRoomRepository", userRoomRepository);
        ReflectionTestUtils.setField(
                roomService, "roomIdGenerator", new AtomicLong(0)); // ID 생성기 초기화
        ReflectionTestUtils.setField(roomService, "lockExecutor", lockExecutor);
        ReflectionTestUtils.setField(roomService, "quizService", quizService);

        Quiz dummyQuiz = mock(Quiz.class);
        when(dummyQuiz.getId()).thenReturn(1L);
        when(quizService.getQuizWithQuestionsById(anyLong())).thenReturn(dummyQuiz);
        when(quizService.getQuizMinData()).thenReturn(new QuizMinData(1L, 10L));
        doNothing().when(eventPublisher).publishEvent(any());
        doNothing().when(disconnectTasks).cancelDisconnectTask(any(Long.class));
        doNothing().when(messageSender).sendPersonal(any(), any(), any(), any());
        doNothing().when(messageSender).sendBroadcast(any(), any(), any());
    }

    // --- 테스트 시나리오 시작 ---

    @Test
    @DisplayName("다수의 사용자가 동시 입장 시도 시 데드락 없이 정합성 유지")
    void enterRoom_concurrently_noDeadlockAndConsistency() throws InterruptedException {
        // Given
        Long roomId = 1L;
        int maxUserCount = 4;
        Long hostId = 100L;
        String hostNickname = "host";

        // Room 생성 (호스트 포함 1명)
        createAndSaveRoom(roomId, 100L, hostNickname, maxUserCount);

        int numConcurrentUsers = 10; // 동시 입장 시도할 사용자 수
        ExecutorService executorService = Executors.newFixedThreadPool(numConcurrentUsers);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numConcurrentUsers);

        AtomicInteger successEntries = new AtomicInteger(0);
        AtomicInteger failedEntries = new AtomicInteger(0);

        // When
        for (int i = 0; i < numConcurrentUsers; i++) {
            long userId = (long) i + 101; // 호스트와 겹치지 않게

            UserPrincipal userPrincipal = createUserPrincipal(userId);

            RoomValidationRequest request = new RoomValidationRequest(roomId, null);

            executorService.submit(
                    () -> {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userPrincipal, null, Collections.emptyList());
                        SecurityContext context = SecurityContextHolder.createEmptyContext();
                        context.setAuthentication(authentication);
                        SecurityContextHolder.setContext(context);

                        try {
                            startLatch.await(); // 모든 스레드가 동시에 시작하도록 대기
                            roomService.enterRoom(request);
                            successEntries.incrementAndGet();
                        } catch (CustomException e) {
                            if (e.getErrorCode() == RoomErrorCode.ROOM_USER_LIMIT_REACHED) {
                                failedEntries.incrementAndGet();
                            } else {
                                log.info(
                                        "Unexpected CustomException in enterRoom for user {}: {}",
                                        userPrincipal.getUserId(),
                                        e.getMessage(),
                                        e);
                                failedEntries.incrementAndGet();
                            }
                        } catch (Exception e) {
                            log.info(
                                    "Unhandled Exception in enterRoom for user {}: {}",
                                    userPrincipal.getUserId(),
                                    e.getMessage(),
                                    e); // 여기에 예외 상세 로그
                            failedEntries.incrementAndGet();
                        } finally {
                            SecurityContextHolder.clearContext();
                            finishLatch.countDown();
                        }
                    });
        }

        startLatch.countDown(); // 모든 스레드 시작!
        assertThat(finishLatch.await(50, TimeUnit.SECONDS)).isTrue(); // 모든 스레드 완료 대기

        executorService.shutdown();
        assertThat(executorService.awaitTermination(1, TimeUnit.MINUTES)).isTrue(); // 데드락 방지 확인

        // Then
        // 호스트 1명 + 성공적인 입장 플레이어 = 최대 인원수
        assertThat(successEntries.get()).isEqualTo(maxUserCount - 1);
        assertThat(failedEntries.get()).isEqualTo(numConcurrentUsers - (maxUserCount - 1));

        Room finalRoom = roomRepository.findRoom(roomId).orElseThrow();
        assertThat(finalRoom.getCurrentUserCnt()).isEqualTo(maxUserCount); // 최종 인원수 확인
        assertThat(finalRoom.getPlayerMap().size()).isEqualTo(maxUserCount); // 플레이어 맵 사이즈 확인

        for (long i = 0; i < numConcurrentUsers; i++) {
            long userId = (long) i + 101;
            if (userRoomRepository.isUserInAnyRoom(userId)) {
                assertThat(userRoomRepository.getRoomId(userId)).isEqualTo(roomId);
            }
        }
    }

    @Test
    @DisplayName("동일 유저가 여러 탭에서 동시 초기화 요청 시 데드락 없이 처리되고, 최종적으로 하나의 방에만 존재 (가장 마지막에 시도한 방)")
    void initializeRoomSocket_concurrently_sameUser_noDeadlockAndConsistency()
            throws InterruptedException {
        // Given
        Long roomId1 = 1L;
        Long roomId2 = 2L;
        Long testUserId = 1000L; // 테스트 대상 유저 ID
        String nickname = "TestUser";
        int maxUserCount = 4;

        UserPrincipal userPrincipal = createUserPrincipal(testUserId);

        // 방 1 생성
        Room room1 = createAndSaveRoom(roomId1, 2000L, "Host1", maxUserCount);

        // 방 2 생성
        Room room2 = createAndSaveRoom(roomId2, 3000L, "Host2", maxUserCount);

        Player testPlayer = createPlayer(testUserId, nickname);
        roomRepository.findRoom(roomId1).orElseThrow().addPlayer(testPlayer);
        userRoomRepository.addUser(testPlayer, room1);

        int numAttempts = 5; // 각 방에 대한 동시 초기화 요청 횟수
        // 총 스레드 수는 numAttempts * 2 (room1에 대한 numAttempts, room2에 대한 numAttempts)
        ExecutorService executorService = Executors.newFixedThreadPool(numAttempts * 2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numAttempts * 2);

        AtomicInteger successCount = new AtomicInteger(0);
        // 가장 마지막에 성공적으로 초기화된 방의 ID를 추적
        List<AbstractMap.SimpleEntry<Long, Long>> successfulInitAttempts =
                Collections.synchronizedList(new ArrayList<>());

        // When
        for (int i = 0; i < numAttempts; i++) {
            // Room1 초기화 시도
            executorService.submit(
                    () -> {
                        setSecurityContext(userPrincipal);
                        try {
                            startLatch.await();
                            long callTimestamp = System.nanoTime(); // 호출 시작 시간 기록
                            roomService.initializeRoomSocket(roomId1, userPrincipal);
                            successCount.incrementAndGet();
                            successfulInitAttempts.add(
                                    new AbstractMap.SimpleEntry<>(roomId1, callTimestamp));
                        } catch (Exception e) {
                            log.error(
                                    "Room1 init failed for user {} in room {}: {}",
                                    userPrincipal.getUserId(),
                                    roomId1,
                                    e.getMessage(),
                                    e);
                        } finally {
                            SecurityContextHolder.clearContext();
                            finishLatch.countDown();
                        }
                    });

            // Room2 초기화 시도
            executorService.submit(
                    () -> {
                        setSecurityContext(userPrincipal);
                        try {
                            startLatch.await();
                            long callTimestamp = System.nanoTime(); // 호출 시작 시간 기록
                            roomService.initializeRoomSocket(roomId2, userPrincipal);
                            successCount.incrementAndGet();
                            successfulInitAttempts.add(
                                    new AbstractMap.SimpleEntry<>(roomId2, callTimestamp));
                        } catch (Exception e) {
                            log.error(
                                    "Room2 init failed for user {} in room {}: {}",
                                    userPrincipal.getUserId(),
                                    roomId2,
                                    e.getMessage(),
                                    e);
                        } finally {
                            SecurityContextHolder.clearContext();
                            finishLatch.countDown();
                        }
                    });
        }

        startLatch.countDown();
        assertThat(finishLatch.await(50, TimeUnit.SECONDS)).isTrue(); // 시간 충분히 늘림
        executorService.shutdown();
        assertThat(executorService.awaitTermination(1, TimeUnit.MINUTES)).isTrue();

        // Then
        // 최종적으로 유저는 하나의 방에만 존재해야 함
        assertThat(userRoomRepository.isUserInAnyRoom(testUserId)).isTrue();
        Long finalRoomId = userRoomRepository.getRoomId(testUserId);
        assertThat(finalRoomId).isNotNull();

        // 마지막으로 성공적으로 initializeRoomSocket이 호출된 방을 찾음
        successfulInitAttempts.sort(Comparator.comparing(AbstractMap.SimpleEntry::getValue));
        Long expectedFinalRoomId = null;
        if (!successfulInitAttempts.isEmpty()) {
            expectedFinalRoomId =
                    successfulInitAttempts.get(successfulInitAttempts.size() - 1).getKey();
        }

        // 최종적으로 저장된 방 ID가 가장 마지막에 성공적으로 시도된 방 ID와 일치하는지 검증
        assertThat(finalRoomId)
                .as("Final room must be the one from the last successful initialization attempt")
                .isEqualTo(expectedFinalRoomId);

        // 각 방의 상태 검증
        Room finalRoom1 = roomRepository.findRoom(roomId1).orElse(null);
        Room finalRoom2 = roomRepository.findRoom(roomId2).orElse(null);

        // 마지막 방에만 유저가 남아있는지 확인
        if (finalRoomId.equals(roomId1)) {
            assertThat(finalRoom1).isNotNull();
            assertThat(finalRoom1.hasPlayer(testUserId)).isTrue();
            // 다른 방에는 유저가 없어야 함
            assertThat(finalRoom2 == null || !finalRoom2.hasPlayer(testUserId)).isTrue();
            // 룸 카운트도 갱신되었는지 확인 (호스트 + 최종 유저)
            assertThat(finalRoom1.getCurrentUserCnt()).isEqualTo(2); // 호스트1 + 테스트유저1
        } else if (finalRoomId.equals(roomId2)) {
            assertThat(finalRoom2).isNotNull();
            assertThat(finalRoom2.hasPlayer(testUserId)).isTrue();
            // 다른 방에는 유저가 없어야 함
            assertThat(finalRoom1 == null || !finalRoom1.hasPlayer(testUserId)).isTrue();
            // 룸 카운트도 갱신되었는지 확인 (호스트 + 최종 유저)
            assertThat(finalRoom2.getCurrentUserCnt()).isEqualTo(2); // 호스트1 + 테스트유저1
        } else {
            // 이 경우는 발생해서는 안 됨 (userId가 두 방 중 하나에만 있어야 하므로)
            assertThat(false).as("User must be in either room1 or room2").isTrue();
        }
    }

    @Test
    @DisplayName("다수의 사용자가 동시 입장/나가기 시도 시 데드락 없이 정합성 유지")
    void enterAndExit_concurrently_noDeadlockAndConsistency() throws InterruptedException {
        // Given
        Long roomId = 1L;
        Long hostId = 100L;
        int maxUserCount = 4;

        // Room 생성 (호스트 포함 1명)
        createAndSaveRoom(roomId, hostId, "Host", maxUserCount);

        int numUsers = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(numUsers * 2); // 입장과 나가기 스레드
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numUsers * 2);

        // When
        for (int i = 0; i < numUsers; i++) {
            long userId = (long) i + 201;
            UserPrincipal userPrincipal = createUserPrincipal(userId);
            RoomValidationRequest enterRequest = new RoomValidationRequest(roomId, null);

            // 입장 스레드
            executorService.submit(
                    () -> {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userPrincipal, null, Collections.emptyList());
                        SecurityContext context = SecurityContextHolder.createEmptyContext();
                        context.setAuthentication(authentication);
                        SecurityContextHolder.setContext(context);
                        try {
                            startLatch.await();
                            roomService.enterRoom(enterRequest);
                        } catch (Exception e) {
                            // 예상되는 예외: ROOM_USER_LIMIT_REACHED
                        } finally {
                            SecurityContextHolder.clearContext();
                            finishLatch.countDown();
                        }
                    });

            // 나가기 스레드 (입장 시도 후 바로 나가기 시도)
            executorService.submit(
                    () -> {
                        setSecurityContext(userPrincipal);
                        try {
                            startLatch.await();
                            roomService.exitRoomWithLock(roomId, userPrincipal);
                        } catch (Exception e) {
                            // 예상되는 예외: USER_NOT_FOUND (아직 입장하지 못했을 때)
                        } finally {
                            SecurityContextHolder.clearContext();
                            finishLatch.countDown();
                        }
                    });
        }

        startLatch.countDown();
        assertThat(finishLatch.await(20, TimeUnit.SECONDS)).isTrue();
        executorService.shutdown();
        assertThat(executorService.awaitTermination(1, TimeUnit.MINUTES)).isTrue();

        // Then
        // 최종적으로 호스트만 남아있거나, 소수의 플레이어가 남아있을 수 있음
        // 중요한 것은 데드락 없이 완료되고 시스템이 불안정한 상태가 되지 않는 것
        Room finalRoom = roomRepository.findRoom(roomId).orElseThrow();
        assertThat(finalRoom.hasPlayer(hostId)).isTrue(); // 호스트는 남아있어야 함
        // 추가적인 플레이어의 수는 동시성 상황에 따라 유동적일 수 있음.
        // 예를 들어, 어떤 유저가 입장 직후 바로 나가는 데 성공하면 0이 될 수도 있고,
        // 어떤 유저가 입장만 성공하고 나가기는 실패할 수도 있음.
        // 여기서는 데드락이 없고, 불필요한 예외가 발생하지 않으며, 최소한의 정합성(호스트 존재)만 확인.
        System.out.println(
                "Final user count in room " + roomId + ": " + finalRoom.getCurrentUserCnt());
    }

    @Test
    @DisplayName("연결 끊긴 플레이어 처리 로직과 사용자 직접 나가기 로직 동시 호출 시 데드락 없음")
    void disconnectAndExit_concurrently_noDeadlock() throws InterruptedException {
        // Given
        Long roomId = 1L;
        Long hostId = 100L;
        Long disconnectedUserId = 200L;
        Long exitingUserId = 300L;

        // 방 생성 및 플레이어 추가
        int maxUserCount = 4;

        // Room 생성
        // 방 생성 및 플레이어 추가 - 헬퍼 메서드 사용
        Room room = createAndSaveRoom(roomId, hostId, "Host", maxUserCount);
        Player disconnectedPlayer =
                createPlayer(disconnectedUserId, "DisconnectedUser"); // 헬퍼 메서드 사용
        Player exitingPlayer = createPlayer(exitingUserId, "ExitingUser"); // 헬퍼 메서드 사용

        room.addPlayer(disconnectedPlayer);
        room.addPlayer(exitingPlayer);

        // UserRoomRepository 매핑
        userRoomRepository.addUser(disconnectedPlayer, room);
        userRoomRepository.addUser(exitingPlayer, room);

        // disconnectOrExitRoom 호출 시 changeConnectedStatus를 간접적으로 테스트하기 위해 Mock 설정
        // 이 부분은 room.updatePlayerConnectionState를 직접 호출하므로 Mocking 불필요.
        // userRoomRepository.removeUser는 In-memory 구현체를 사용하므로 Mocking 불필요.

        int numConcurrentCalls = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(numConcurrentCalls * 2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numConcurrentCalls * 2);

        // When
        for (int i = 0; i < numConcurrentCalls; i++) {
            long userId = (long) i + 201;
            UserPrincipal disconnectedUserPrincipal = createUserPrincipal(disconnectedUserId);
            // disconnectOrExitRoom (플레이어 연결 끊김 시뮬레이션)
            executorService.submit(
                    () -> {
                        setSecurityContext(disconnectedUserPrincipal);
                        try {
                            startLatch.await();
                            roomService.disconnectOrExitRoom(roomId, disconnectedUserPrincipal);
                        } catch (Exception e) {
                            System.err.println("Disconnect error: " + e.getMessage());
                        } finally {
                            SecurityContextHolder.clearContext();
                            finishLatch.countDown();
                        }
                    });

            UserPrincipal exitingUserPrincipal = createUserPrincipal(exitingUserId);

            // exitRoomWithLock (사용자 직접 나가기 시뮬레이션)
            executorService.submit(
                    () -> {
                        setSecurityContext(exitingUserPrincipal);
                        try {
                            startLatch.await();
                            roomService.exitRoomWithLock(roomId, exitingUserPrincipal);
                        } catch (Exception e) {
                            System.err.println("Exit error: " + e.getMessage());
                        } finally {
                            SecurityContextHolder.clearContext();
                            finishLatch.countDown();
                        }
                    });
        }

        startLatch.countDown();
        assertThat(finishLatch.await(20, TimeUnit.SECONDS)).isTrue();
        executorService.shutdown();
        assertThat(executorService.awaitTermination(1, TimeUnit.MINUTES)).isTrue();

        // Then
        Room finalRoom = roomRepository.findRoom(roomId).orElseThrow();
        assertThat(finalRoom.hasPlayer(hostId)).isTrue(); // 호스트는 남아있어야 함

        // disconnectedUser와 exitingUser는 최종적으로 방에서 나가져야 함
        assertThat(finalRoom.hasPlayer(disconnectedUserId)).isFalse();
        assertThat(finalRoom.hasPlayer(exitingUserId)).isFalse();

        assertThat(userRoomRepository.isUserInAnyRoom(disconnectedUserId)).isFalse();
        assertThat(userRoomRepository.isUserInAnyRoom(exitingUserId)).isFalse();
        assertThat(userRoomRepository.isUserInAnyRoom(hostId)).isTrue(); // 호스트는 남아있어야 함

        // 최종 방 인원수 확인 (호스트 1명만 남아야 함)
        assertThat(finalRoom.getCurrentUserCnt()).isEqualTo(1);
    }

    private Player createPlayer(Long userId, String nickname) {
        return new Player(userId, nickname);
    }

    private UserPrincipal createUserPrincipal(Long userId) {
        User user = new User("kakao", "providerId_" + userId, LocalDateTime.now());
        ReflectionTestUtils.setField(user, "id", userId);
        return new UserPrincipal(user, Collections.emptyMap());
    }

    private Room createAndSaveRoom(
            Long roomId, Long hostId, String hostNickname, int maxUserCount) {
        RoomSetting roomSetting = new RoomSetting("testRoom", maxUserCount, false, null);
        Player host = createPlayer(hostId, hostNickname);
        Room room = new Room(roomId, roomSetting, new GameSetting(1L, 10, 3), host);
        room.addPlayer(host);
        roomRepository.saveRoom(room);
        userRoomRepository.addUser(host, room);
        return room;
    }

    private void setSecurityContext(UserPrincipal userPrincipal) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userPrincipal, null, Collections.emptyList());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    // --- 인메모리 저장소 구현체 (테스트용) ---

    // RoomRepository의 인메모리 구현체
    private static class InMemoryRoomRepository implements RoomRepository {
        private final Map<Long, Room> rooms = new ConcurrentHashMap<>();

        @Override
        public void saveRoom(Room room) {
            rooms.put(room.getId(), room);
        }

        @Override
        public Optional<Room> findRoom(Long roomId) {
            return Optional.ofNullable(rooms.get(roomId));
        }

        @Override
        public List<Room> findAll() {
            return rooms.values().stream().toList();
        }

        @Override
        public void removeRoom(Long roomId) {
            rooms.remove(roomId);
        }
    }
}
