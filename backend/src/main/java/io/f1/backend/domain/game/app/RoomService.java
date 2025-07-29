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
import static io.f1.backend.domain.quiz.mapper.QuizMapper.toGameStartResponse;
import static io.f1.backend.global.util.SecurityUtils.getCurrentUserId;
import static io.f1.backend.global.util.SecurityUtils.getCurrentUserNickname;
import static io.f1.backend.global.util.SecurityUtils.getCurrentUserPrincipal;

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
import io.f1.backend.domain.game.model.ConnectionState;
import io.f1.backend.domain.game.model.GameSetting;
import io.f1.backend.domain.game.model.Player;
import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.game.model.RoomSetting;
import io.f1.backend.domain.game.model.RoomState;
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
    private final Map<Long, Object> roomLocks = new ConcurrentHashMap<>();
    private final DisconnectTaskManager disconnectTasks;

    private final MessageSender messageSender;

    private static final int CONTINUE_DELAY = 3;

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
        exitIfInAnotherRoom(room, host.getId());

        eventPublisher.publishEvent(new RoomCreatedEvent(room, quiz));

        return new RoomCreateResponse(newId);
    }

    public void enterRoom(RoomValidationRequest request) {

        Long roomId = request.roomId();

        Object lock = roomLocks.computeIfAbsent(roomId, k -> new Object());

        synchronized (lock) {
            Room room = findRoom(request.roomId());

            Long userId = getCurrentUserId();

            /* 다른 방 접속 시 기존 방은 exit 처리 - 탭 동시 로그인 시 (disconnected 리스너 작동x)  */
            exitIfInAnotherRoom(room, userId);

            /* reconnect */
            if (room.hasPlayer(userId)) {
                return;
            }

            if (room.getState().equals(RoomState.PLAYING)) {
                throw new CustomException(RoomErrorCode.ROOM_GAME_IN_PROGRESS);
            }

            int maxUserCnt = room.getRoomSetting().maxUserCount();
            int currentCnt = room.getCurrentUserCnt();
            if (maxUserCnt == currentCnt) {
                throw new CustomException(RoomErrorCode.ROOM_USER_LIMIT_REACHED);
            }

            if (room.getRoomSetting().locked()
                    && !room.getRoomSetting().password().equals(request.password())) {
                throw new CustomException(RoomErrorCode.WRONG_PASSWORD);
            }

            room.addPlayer(createPlayer());
        }
    }

    private void exitIfInAnotherRoom(Room room, Long userId) {

        Long joinedRoomId = userRoomRepository.getRoomId(userId);

        if (joinedRoomId != null && !room.getId().equals(joinedRoomId)) {

            if (room.isPlaying()) {
                changeConnectedStatus(userId, ConnectionState.DISCONNECTED);
            } else {
                exitRoom(joinedRoomId, getCurrentUserPrincipal());
            }
        }
    }

    public void initializeRoomSocket(Long roomId, UserPrincipal principal) {

        Room room = findRoom(roomId);
        Long userId = principal.getUserId();

        if (!room.hasPlayer(userId)) {
            throw new CustomException(RoomErrorCode.ROOM_ENTER_REQUIRED);
        }

        /* 재연결 */
        if (room.getPlayerState(userId).equals(ConnectionState.DISCONNECTED)) {
            changeConnectedStatus(userId, ConnectionState.CONNECTED);
            cancelTask(userId);
            reconnectSendResponse(roomId, principal);
            return;
        }

        Player player = createPlayer(principal);

        RoomSettingResponse roomSettingResponse = toRoomSettingResponse(room);

        Long quizId = room.getGameSetting().getQuizId();
        Quiz quiz = quizService.getQuizWithQuestionsById(quizId);

        GameSettingResponse gameSettingResponse =
                toGameSettingResponse(room.getGameSetting(), quiz);

        PlayerListResponse playerListResponse = toPlayerListResponse(room);

        SystemNoticeResponse systemNoticeResponse =
                ofPlayerEvent(player.getNickname(), RoomEventType.ENTER);

        String destination = getDestination(roomId);

        userRoomRepository.addUser(player, room);

        messageSender.sendPersonal(
                getUserDestination(), MessageType.GAME_SETTING, gameSettingResponse, principal);

        messageSender.sendBroadcast(destination, MessageType.ROOM_SETTING, roomSettingResponse);
        messageSender.sendBroadcast(destination, MessageType.PLAYER_LIST, playerListResponse);
        messageSender.sendBroadcast(destination, MessageType.SYSTEM_NOTICE, systemNoticeResponse);
    }

    public void exitRoom(Long roomId, UserPrincipal principal) {

        Object lock = roomLocks.computeIfAbsent(roomId, k -> new Object());

        synchronized (lock) {
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
                    principal);

            SystemNoticeResponse systemNoticeResponse =
                    ofPlayerEvent(removePlayer.nickname, RoomEventType.EXIT);

            PlayerListResponse playerListResponse = toPlayerListResponse(room);

            messageSender.sendBroadcast(destination, MessageType.PLAYER_LIST, playerListResponse);
            messageSender.sendBroadcast(
                    destination, MessageType.SYSTEM_NOTICE, systemNoticeResponse);
        }
    }

    public RoomListResponse getAllRooms() {
        List<Room> rooms = roomRepository.findAll();
        List<RoomResponse> roomResponses =
                rooms.stream()
                        .map(
                                room -> {
                                    Long quizId = room.getGameSetting().getQuizId();
                                    Quiz quiz = quizService.getQuizWithQuestionsById(quizId);

                                    return toRoomResponse(room, quiz);
                                })
                        .toList();
        return new RoomListResponse(roomResponses);
    }

    public void reconnectSendResponse(Long roomId, UserPrincipal principal) {
        Room room = findRoom(roomId);

        String destination = getDestination(roomId);
        String userDestination = getUserDestination();

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
                    principal);
            messageSender.sendPersonal(
                    userDestination,
                    MessageType.RANK_UPDATE,
                    toRankUpdateResponse(room),
                    principal);
            messageSender.sendPersonal(
                    userDestination,
                    MessageType.GAME_START,
                    toGameStartResponse(room.getQuestions()),
                    principal);
        } else {
            RoomSettingResponse roomSettingResponse = toRoomSettingResponse(room);

            Long quizId = room.getGameSetting().getQuizId();

            Quiz quiz = quizService.getQuizWithQuestionsById(quizId);

            GameSettingResponse gameSettingResponse =
                    toGameSettingResponse(room.getGameSetting(), quiz);

            PlayerListResponse playerListResponse = toPlayerListResponse(room);

            messageSender.sendPersonal(
                    userDestination, MessageType.ROOM_SETTING, roomSettingResponse, principal);
            messageSender.sendPersonal(
                    userDestination, MessageType.PLAYER_LIST, playerListResponse, principal);
            messageSender.sendPersonal(
                    userDestination, MessageType.GAME_SETTING, gameSettingResponse, principal);
        }
    }

    public Long changeConnectedStatus(Long userId, ConnectionState newState) {
        Long roomId = userRoomRepository.getRoomId(userId);
        Room room = findRoom(roomId);

        room.updatePlayerConnectionState(userId, newState);

        return roomId;
    }

    public void cancelTask(Long userId) {
        disconnectTasks.cancelDisconnectTask(userId);
    }

    public void exitIfNotPlaying(Long roomId, UserPrincipal principal) {
        Room room = findRoom(roomId);
        if (room.isPlaying()) {
            removeUserRepository(principal.getUserId(), roomId);
        } else {
            exitRoom(roomId, principal);
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

    private void removeRoom(Room room) {
        Long roomId = room.getId();
        roomRepository.removeRoom(roomId);
        roomLocks.remove(roomId);
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

    private String getUserDestination() {
        return "/queue";
    }

    public void exitRoomForDisconnectedPlayer(Long roomId, Player player) {

        Object lock = roomLocks.computeIfAbsent(roomId, k -> new Object());

        synchronized (lock) {
            // 연결 끊긴 플레이어 exit 로직 타게 해주기
            Room room = findRoom(roomId);

            cleanRoom(room, player);

            String destination = getDestination(roomId);

            SystemNoticeResponse systemNoticeResponse =
                    ofPlayerEvent(player.nickname, RoomEventType.EXIT);

            messageSender.sendBroadcast(
                    destination, MessageType.SYSTEM_NOTICE, systemNoticeResponse);
            messageSender.sendBroadcast(
                    destination, MessageType.PLAYER_LIST, toPlayerListResponse(room));
        }
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

    public boolean isUserInAnyRoom(Long userId) {
        return userRoomRepository.isUserInAnyRoom(userId);
    }
}
