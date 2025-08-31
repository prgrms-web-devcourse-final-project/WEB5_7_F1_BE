package io.f1.backend.domain.game.app;

import static io.f1.backend.domain.game.mapper.RoomMapper.ofPlayerEvent;
import static io.f1.backend.domain.game.mapper.RoomMapper.toGameSetting;
import static io.f1.backend.domain.game.mapper.RoomMapper.toGameSettingResponse;
import static io.f1.backend.domain.game.mapper.RoomMapper.toPlayerListResponse;
import static io.f1.backend.domain.game.mapper.RoomMapper.toRankUpdateResponse;
import static io.f1.backend.domain.game.mapper.RoomMapper.toRoomResponse;
import static io.f1.backend.domain.game.mapper.RoomMapper.toRoomSetting;
import static io.f1.backend.domain.game.mapper.RoomMapper.toRoomSettingResponse;
import static io.f1.backend.domain.game.websocket.WebSocketUtils.getDestination;
import static io.f1.backend.domain.game.websocket.WebSocketUtils.getUserDestination;
import static io.f1.backend.domain.quiz.mapper.QuizMapper.toGameStartResponse;
import static io.f1.backend.global.security.util.SecurityUtils.getCurrentUserId;
import static io.f1.backend.global.security.util.SecurityUtils.getCurrentUserNickname;
import static io.f1.backend.global.security.util.SecurityUtils.getCurrentUserPrincipal;

import io.f1.backend.domain.game.dto.MessageType;
import io.f1.backend.domain.game.dto.RoomEventType;
import io.f1.backend.domain.game.dto.request.RoomCreateRequest;
import io.f1.backend.domain.game.dto.request.RoomValidationRequest;
import io.f1.backend.domain.game.dto.response.ExitSuccessResponse;
import io.f1.backend.domain.game.dto.response.GameSettingResponse;
import io.f1.backend.domain.game.dto.response.PlayerListResponse;
import io.f1.backend.domain.game.dto.response.RoomCreateResponse;
import io.f1.backend.domain.game.dto.response.RoomListResponse;
import io.f1.backend.domain.game.dto.response.RoomResponse;
import io.f1.backend.domain.game.dto.response.RoomSettingResponse;
import io.f1.backend.domain.game.dto.response.SystemNoticeResponse;
import io.f1.backend.domain.game.event.RoomCreatedEvent;
import io.f1.backend.domain.game.event.RoomDeletedEvent;
import io.f1.backend.domain.game.event.RoomUpdatedEvent;
import io.f1.backend.domain.game.model.ConnectionState;
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
import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.exception.errorcode.RoomErrorCode;
import io.f1.backend.global.exception.errorcode.UserErrorCode;
import io.f1.backend.global.lock.DistributedLock;
import io.f1.backend.global.lock.LockExecutor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final QuizService quizService;
    private final RoomRepository roomRepository;
    private final UserRoomRepository userRoomRepository;
    private final AtomicLong roomIdGenerator = new AtomicLong(0);
    private final ApplicationEventPublisher eventPublisher;
    private final Map<String, Long> sessionRoomMap = new ConcurrentHashMap<>();

    private final DisconnectTaskManager disconnectTasks;
    private final MessageSender messageSender;
    private final LockExecutor lockExecutor;

    public static final String ROOM_LOCK_PREFIX = "room";
    public static final String USER_LOCK_PREFIX = "user";

    public RoomCreateResponse saveRoom(RoomCreateRequest request) {

        QuizMinData quizMinData = quizService.getQuizMinData();

        Quiz quiz = quizService.findQuizById(quizMinData.quizMinId());

        GameSetting gameSetting = toGameSetting(quizMinData);

        Player host = createPlayer();

        RoomSetting roomSetting = toRoomSetting(request);

        Long newId = roomIdGenerator.incrementAndGet();

        Room room = new Room(newId, roomSetting, gameSetting, host);

        room.addPlayer(host);

        roomRepository.saveRoom(room);

        /* 다른 방 접속 시 기존 방은 exit 처리 - 탭 동시 로그인 시 (disconnected 리스너 작동x)  */
        lockExecutor.executeWithLock(
                USER_LOCK_PREFIX,
                host.getId(),
                () -> exitIfInAnotherRoom(room, getCurrentUserPrincipal()));

        eventPublisher.publishEvent(new RoomCreatedEvent(room, quiz, gameSetting.getRound()));

        return new RoomCreateResponse(newId);
    }

    public void enterRoom(RoomValidationRequest request) {

        Long roomId = request.roomId();

        Room room = findRoom(roomId);

        /* 다른 방 접속 시 기존 방은 exit 처리 - 탭 동시 로그인 시 (disconnected 리스너 작동x)  */
        lockExecutor.executeWithLock(
                USER_LOCK_PREFIX,
                getCurrentUserId(),
                () -> exitIfInAnotherRoom(room, getCurrentUserPrincipal()));

        lockExecutor.executeWithLock(
                ROOM_LOCK_PREFIX, roomId, () -> performEnterRoomLogic(request));
    }

    private void performEnterRoomLogic(RoomValidationRequest request) {

        Long roomId = request.roomId();

        Room room = findRoom(roomId);

        Long userId = getCurrentUserId();

        /* reconnect */
        if (room.hasPlayer(userId)) {
            return;
        }

        if (room.isPlaying()) {
            throw new CustomException(RoomErrorCode.ROOM_GAME_IN_PROGRESS);
        }

        int maxUserCnt = room.getRoomSetting().maxUserCount();
        int currentCnt = room.getCurrentUserCnt();
        if (maxUserCnt == currentCnt) {
            throw new CustomException(RoomErrorCode.ROOM_USER_LIMIT_REACHED);
        }

        if (room.isPasswordIncorrect(request.password())) {
            throw new CustomException(RoomErrorCode.WRONG_PASSWORD);
        }

        room.addPlayer(createPlayer());
    }

    private void exitIfInAnotherRoom(Room room, UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getUserId();
        Long joinedRoomId = getRoomIdByUserId(userId);

        if (joinedRoomId != null && !room.isSameRoom(joinedRoomId)) {
            lockExecutor.executeWithLock(
                    ROOM_LOCK_PREFIX,
                    joinedRoomId,
                    () -> disconnectOrExitRoom(joinedRoomId, userPrincipal));
        }
    }

    public void initializeRoomSocket(Long roomId, UserPrincipal principal) {

        Long userId = principal.getUserId();

        lockExecutor.executeWithLock(
                USER_LOCK_PREFIX,
                userId,
                () -> {
                    lockExecutor.executeWithLock(
                            ROOM_LOCK_PREFIX,
                            roomId,
                            () -> {
                                Room room = findRoom(roomId);

                                if (!room.hasPlayer(userId)) {
                                    throw new CustomException(RoomErrorCode.ROOM_ENTER_REQUIRED);
                                }

                                /* 재연결 */
                                if (room.isPlayerInState(userId, ConnectionState.DISCONNECTED)) {
                                    changeConnectedStatus(
                                            roomId, userId, ConnectionState.CONNECTED);
                                    cancelTask(userId);
                                    reconnectSendResponse(roomId, principal);
                                    return;
                                }

                                Player player = createPlayer(principal);

                                RoomSettingResponse roomSettingResponse =
                                        toRoomSettingResponse(room);

                                Long quizId = room.getGameSetting().getQuizId();
                                Quiz quiz = quizService.getQuizWithQuestionsById(quizId);

                                GameSettingResponse gameSettingResponse =
                                        toGameSettingResponse(
                                                room.getGameSetting(),
                                                quiz,
                                                quiz.getQuestions().size());

                                PlayerListResponse playerListResponse = toPlayerListResponse(room);

                                SystemNoticeResponse systemNoticeResponse =
                                        ofPlayerEvent(player.getNickname(), RoomEventType.ENTER);

                                String destination = getDestination(roomId);

                                userRoomRepository.addUser(player, room);

                                messageSender.sendPersonal(
                                        getUserDestination(),
                                        MessageType.GAME_SETTING,
                                        gameSettingResponse,
                                        principal.getName());

                                messageSender.sendBroadcast(
                                        destination, MessageType.ROOM_SETTING, roomSettingResponse);
                                messageSender.sendBroadcast(
                                        destination, MessageType.PLAYER_LIST, playerListResponse);
                                messageSender.sendBroadcast(
                                        destination,
                                        MessageType.SYSTEM_NOTICE,
                                        systemNoticeResponse);

                                eventPublisher.publishEvent(
                                        new RoomUpdatedEvent(
                                                room, quiz, quiz.getQuestions().size()));
                            });
                });
    }

    public void exitRoomWithLock(Long roomId, UserPrincipal principal) {
        lockExecutor.executeWithLock(
                USER_LOCK_PREFIX,
                principal.getUserId(),
                () -> {
                    lockExecutor.executeWithLock(
                            ROOM_LOCK_PREFIX,
                            roomId,
                            () -> {
                                exitRoom(roomId, principal);
                            });
                });
    }

    private void exitRoom(Long roomId, UserPrincipal principal) {

        Room room = findRoom(roomId);

        if (!room.hasPlayer(principal.getUserId())) {
            throw new CustomException(UserErrorCode.USER_NOT_FOUND);
        }

        Player removePlayer = createPlayer(principal);

        String destination = getDestination(roomId);

        cleanRoom(room, removePlayer);

        messageSender.sendPersonal(
                getUserDestination(),
                MessageType.EXIT_SUCCESS,
                new ExitSuccessResponse(true),
                principal.getName());

        SystemNoticeResponse systemNoticeResponse =
                ofPlayerEvent(removePlayer.nickname, RoomEventType.EXIT);

        PlayerListResponse playerListResponse = toPlayerListResponse(room);

        messageSender.sendBroadcast(destination, MessageType.PLAYER_LIST, playerListResponse);
        messageSender.sendBroadcast(destination, MessageType.SYSTEM_NOTICE, systemNoticeResponse);
    }

    public RoomListResponse getAllRooms() {
        List<Room> rooms = roomRepository.findAll();
        List<RoomResponse> roomResponses =
                rooms.stream()
                        .map(
                                room -> {
                                    Long quizId = room.getGameSetting().getQuizId();
                                    Quiz quiz = quizService.findQuizById(quizId);
                                    Long questionsCount = quizService.getQuestionsCount(quizId);
                                    return toRoomResponse(room, quiz, questionsCount);
                                })
                        .toList();
        return new RoomListResponse(roomResponses);
    }

    @DistributedLock(prefix = "room", key = "#roomId")
    public void reconnectSendResponseWithLock(Long roomId, UserPrincipal principal) {
        reconnectSendResponse(roomId, principal);
    }

    public void reconnectSendResponse(Long roomId, UserPrincipal principal) {
        Room room = findRoom(roomId);

        String destination = getDestination(roomId);
        String userDestination = getUserDestination();

        Long quizId = room.getQuizId();
        Quiz quiz = quizService.findQuizById(quizId);

        messageSender.sendBroadcast(
                destination,
                MessageType.SYSTEM_NOTICE,
                ofPlayerEvent(principal.getUserNickname(), RoomEventType.RECONNECT));

        if (room.isPlaying()) {
            messageSender.sendPersonal(
                    userDestination,
                    MessageType.SYSTEM_NOTICE,
                    ofPlayerEvent(
                            principal.getUserNickname(), RoomEventType.RECONNECT_PRIVATE_NOTICE),
                    principal.getName());
            messageSender.sendPersonal(
                    userDestination,
                    MessageType.RANK_UPDATE,
                    toRankUpdateResponse(room),
                    principal.getName());
            messageSender.sendPersonal(
                    userDestination,
                    MessageType.GAME_START,
                    toGameStartResponse(quiz.getQuizType(), room.getQuestions()),
                    principal.getName());
        } else {
            RoomSettingResponse roomSettingResponse = toRoomSettingResponse(room);

            Long questionsCount = quizService.getQuestionsCount(quizId);

            GameSettingResponse gameSettingResponse =
                    toGameSettingResponse(room.getGameSetting(), quiz, questionsCount);

            PlayerListResponse playerListResponse = toPlayerListResponse(room);

            messageSender.sendPersonal(
                    userDestination,
                    MessageType.ROOM_SETTING,
                    roomSettingResponse,
                    principal.getName());
            messageSender.sendPersonal(
                    userDestination,
                    MessageType.PLAYER_LIST,
                    playerListResponse,
                    principal.getName());
            messageSender.sendPersonal(
                    userDestination,
                    MessageType.GAME_SETTING,
                    gameSettingResponse,
                    principal.getName());
        }
    }

    @DistributedLock(prefix = "room", key = "#roomId")
    public void changeConnectedStatusWithLock(Long roomId, Long userId, ConnectionState newState) {
        changeConnectedStatus(roomId, userId, newState);
    }

    public void changeConnectedStatus(Long roomId, Long userId, ConnectionState newState) {
        Room room = findRoom(roomId);
        room.updatePlayerConnectionState(userId, newState);
    }

    public void cancelTask(Long userId) {
        disconnectTasks.cancelDisconnectTask(userId);
    }

    public void disconnectOrExitRoom(Long roomId, UserPrincipal principal) {
        Room room = findRoom(roomId);
        if (room.isPlaying()) {
            changeConnectedStatus(
                    room.getId(), principal.getUserId(), ConnectionState.DISCONNECTED);
            removeUserRepository(principal.getUserId(), roomId);
        } else {
            exitRoom(room.getId(), principal);
        }
    }

    private Player createPlayer(UserPrincipal principal) {
        return new Player(principal.getUserId(), principal.getUserNickname());
    }

    private Player createPlayer() {
        return new Player(getCurrentUserId(), getCurrentUserNickname());
    }

    public Room findRoom(Long roomId) {
        return roomRepository
                .findRoom(roomId)
                .orElseThrow(() -> new CustomException(RoomErrorCode.ROOM_NOT_FOUND));
    }

    public boolean existsRoom(Long roomId) {
        return roomRepository.findRoom(roomId).isPresent();
    }

    private void removeRoom(Room room) {
        Long roomId = room.getId();
        roomRepository.removeRoom(roomId);
        log.info("{}번 방 삭제", roomId);
    }

    private void changeHost(Room room, Player host) {
        Map<Long, Player> playerMap = room.getPlayerMap();

        Optional<Player> nextHost =
                playerMap.entrySet().stream()
                        .filter(entry -> !entry.getKey().equals(host.getId()))
                        .filter(entry -> entry.getValue().getState() == ConnectionState.CONNECTED)
                        .map(Map.Entry::getValue)
                        .findFirst();

        room.updateHost(
                nextHost.orElseThrow(() -> new CustomException(RoomErrorCode.PLAYER_NOT_FOUND)));
    }

    public void exitRoomForDisconnectedPlayer(Long roomId, Player player) {
        lockExecutor.executeWithLock(
                USER_LOCK_PREFIX,
                player.getId(),
                () -> {
                    lockExecutor.executeWithLock(
                            ROOM_LOCK_PREFIX,
                            roomId,
                            () -> {
                                // 연결 끊긴 플레이어 exit 로직 타게 해주기
                                Room room = findRoom(roomId);

                                cleanRoom(room, player);

                                String destination = getDestination(roomId);

                                SystemNoticeResponse systemNoticeResponse =
                                        ofPlayerEvent(player.nickname, RoomEventType.EXIT);

                                messageSender.sendBroadcast(
                                        destination,
                                        MessageType.SYSTEM_NOTICE,
                                        systemNoticeResponse);
                                messageSender.sendBroadcast(
                                        destination,
                                        MessageType.PLAYER_LIST,
                                        toPlayerListResponse(room));
                            });
                });
    }

    private void cleanRoom(Room room, Player player) {

        Long roomId = room.getId();
        Long userId = player.getId();

        /* user-room mapping 정보 삭제 */
        removeUserRepository(userId, roomId);

        /* 방 삭제 */
        if (room.isLastPlayer(player)) {
            removeRoom(room);
            eventPublisher.publishEvent(new RoomDeletedEvent(roomId));
            return;
        }

        /* 방장 변경 */
        if (room.isHost(userId)) {
            changeHost(room, player);
        }

        /* 플레이어 삭제 */
        room.removePlayer(player);

        Long quizId = room.getQuizId();
        Quiz quiz = quizService.findQuizById(quizId);
        Long questionsCount = quizService.getQuestionsCount(quizId);

        eventPublisher.publishEvent(new RoomUpdatedEvent(room, quiz, questionsCount));
    }

    public void handleDisconnectedPlayers(Room room, List<Player> disconnectedPlayers) {
        for (Player player : disconnectedPlayers) {
            exitRoomForDisconnectedPlayer(room.getId(), player);
        }
    }

    public ConnectionState getPlayerState(Long userId, Long roomId) {
        Room room = findRoom(roomId);
        return room.getPlayerState(userId);
    }

    public void removeUserRepository(Long userId, Long roomId) {
        userRoomRepository.removeUser(userId, roomId);
    }

    @DistributedLock(prefix = "user", key = "#userId")
    public boolean isUserInAnyRoom(Long userId) {
        return userRoomRepository.isUserInAnyRoom(userId);
    }

    private Long getRoomIdByUserId(Long userId) {
        return userRoomRepository.getRoomId(userId);
    }

    @DistributedLock(prefix = "user", key = "#userId")
    public Long getRoomIdByUserIdWithLock(Long userId) {
        return userRoomRepository.getRoomId(userId);
    }

    public void addSessionRoomId(String sessionId, Long roomId) {
        sessionRoomMap.put(sessionId, roomId);
    }

    public Long getRoomIdBySessionId(String sessionId) {
        return sessionRoomMap.get(sessionId);
    }

    public void removeSessionRoomId(String sessionId) {
        sessionRoomMap.remove(sessionId);
    }
}
