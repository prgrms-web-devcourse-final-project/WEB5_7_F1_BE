package io.f1.backend.domain.game.app;

import static io.f1.backend.domain.game.app.GameService.START_DELAY;
import static io.f1.backend.domain.game.mapper.RoomMapper.ofPlayerEvent;
import static io.f1.backend.domain.game.mapper.RoomMapper.toExitSuccessResponse;
import static io.f1.backend.domain.game.mapper.RoomMapper.toGameSetting;
import static io.f1.backend.domain.game.mapper.RoomMapper.toGameSettingResponse;
import static io.f1.backend.domain.game.mapper.RoomMapper.toPlayerListResponse;
import static io.f1.backend.domain.game.mapper.RoomMapper.toQuestionResultResponse;
import static io.f1.backend.domain.game.mapper.RoomMapper.toQuestionStartResponse;
import static io.f1.backend.domain.game.mapper.RoomMapper.toRankUpdateResponse;
import static io.f1.backend.domain.game.mapper.RoomMapper.toRoomResponse;
import static io.f1.backend.domain.game.mapper.RoomMapper.toRoomSetting;
import static io.f1.backend.domain.game.mapper.RoomMapper.toRoomSettingResponse;
import static io.f1.backend.domain.game.websocket.WebSocketUtils.getDestination;
import static io.f1.backend.domain.quiz.mapper.QuizMapper.toGameStartResponse;
import static io.f1.backend.global.util.SecurityUtils.getCurrentUserId;
import static io.f1.backend.global.util.SecurityUtils.getCurrentUserNickname;

import io.f1.backend.domain.game.dto.ChatMessage;
import io.f1.backend.domain.game.dto.MessageType;
import io.f1.backend.domain.game.dto.RoomEventType;
import io.f1.backend.domain.game.dto.request.RoomCreateRequest;
import io.f1.backend.domain.game.dto.request.RoomValidationRequest;
import io.f1.backend.domain.game.dto.response.GameSettingResponse;
import io.f1.backend.domain.game.dto.response.PlayerListResponse;
import io.f1.backend.domain.game.dto.response.RoomCreateResponse;
import io.f1.backend.domain.game.dto.response.RoomListResponse;
import io.f1.backend.domain.game.dto.response.RoomResponse;
import io.f1.backend.domain.game.dto.response.RoomSettingResponse;
import io.f1.backend.domain.game.dto.response.SystemNoticeResponse;
import io.f1.backend.domain.game.event.RoomCreatedEvent;
import io.f1.backend.domain.game.model.ConnectionState;
import io.f1.backend.domain.game.model.GameSetting;
import io.f1.backend.domain.game.model.Player;
import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.game.model.RoomSetting;
import io.f1.backend.domain.game.model.RoomState;
import io.f1.backend.domain.game.store.RoomRepository;
import io.f1.backend.domain.game.websocket.MessageSender;
import io.f1.backend.domain.question.entity.Question;
import io.f1.backend.domain.quiz.app.QuizService;
import io.f1.backend.domain.quiz.dto.QuizMinData;
import io.f1.backend.domain.quiz.entity.Quiz;
import io.f1.backend.domain.user.dto.UserPrincipal;
import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.exception.errorcode.RoomErrorCode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final TimerService timerService;
    private final QuizService quizService;
    private final RoomRepository roomRepository;
    private final AtomicLong roomIdGenerator = new AtomicLong(0);
    private final ApplicationEventPublisher eventPublisher;
    private final Map<Long, Object> roomLocks = new ConcurrentHashMap<>();

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

        room.addValidatedUserId(getCurrentUserId());

        roomRepository.saveRoom(room);

        eventPublisher.publishEvent(new RoomCreatedEvent(room, quiz));

        return new RoomCreateResponse(newId);
    }

    public void enterRoom(RoomValidationRequest request) {

        Long roomId = request.roomId();

        Object lock = roomLocks.computeIfAbsent(roomId, k -> new Object());

        synchronized (lock) {
            Room room = findRoom(request.roomId());

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

            room.addValidatedUserId(getCurrentUserId());
        }
    }

    public void initializeRoomSocket(Long roomId, String sessionId, UserPrincipal principal) {

        Room room = findRoom(roomId);

        Player player = createPlayer(principal);

        room.addPlayer(sessionId, player);

        RoomSettingResponse roomSettingResponse = toRoomSettingResponse(room);

        Long quizId = room.getGameSetting().getQuizId();
        Quiz quiz = quizService.getQuizWithQuestionsById(quizId);

        GameSettingResponse gameSettingResponse =
                toGameSettingResponse(room.getGameSetting(), quiz);

        PlayerListResponse playerListResponse = toPlayerListResponse(room);

        SystemNoticeResponse systemNoticeResponse =
                ofPlayerEvent(player.getNickname(), RoomEventType.ENTER);

        String destination = getDestination(roomId);

        messageSender.sendBroadcast(destination, MessageType.ROOM_SETTING, roomSettingResponse);
        messageSender.sendBroadcast(destination, MessageType.GAME_SETTING, gameSettingResponse);
        messageSender.sendBroadcast(destination, MessageType.PLAYER_LIST, playerListResponse);
        messageSender.sendBroadcast(destination, MessageType.SYSTEM_NOTICE, systemNoticeResponse);
    }

    public void exitRoom(Long roomId, String sessionId, UserPrincipal principal) {

        Object lock = roomLocks.computeIfAbsent(roomId, k -> new Object());

        synchronized (lock) {
            Room room = findRoom(roomId);

            Player removePlayer = getRemovePlayer(room, sessionId, principal);

            String destination = getDestination(roomId);

            long userId = principal.getUserId();

            /* 방 삭제 */
            if (room.isLastPlayer(sessionId)) {
                removeRoom(room);
                return;
            }

            /* 방장 변경 */
            if (room.isHost(removePlayer.getId())) {
                changeHost(room, sessionId);
            }

            /* 플레이어 삭제 */
            removePlayer(room, sessionId, removePlayer);

            SystemNoticeResponse systemNoticeResponse =
                    ofPlayerEvent(removePlayer.nickname, RoomEventType.EXIT);

            PlayerListResponse playerListResponse = toPlayerListResponse(room);

            messageSender.send(destination, MessageType.PLAYER_LIST, playerListResponse);
            messageSender.send(destination, MessageType.SYSTEM_NOTICE, systemNoticeResponse);
        }
    }

    public void handlePlayerReady(Long roomId, String sessionId) {
        Player player =
                roomRepository
                        .findPlayerInRoomBySessionId(roomId, sessionId)
                        .orElseThrow(() -> new CustomException(RoomErrorCode.PLAYER_NOT_FOUND));

        player.toggleReady();

        Room room = findRoom(roomId);

        String destination = getDestination(roomId);

        messageSender.sendBroadcast(destination, MessageType.PLAYER_LIST, toPlayerListResponse(room));
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

    // todo 동시성적용
    public void chat(Long roomId, String sessionId, ChatMessage chatMessage) {
        Room room = findRoom(roomId);

        String destination = getDestination(roomId);

        messageSender.sendBroadcast(destination, MessageType.CHAT, chatMessage);

        if (!room.isPlaying()) {
            return;
        }

        Question currentQuestion = room.getCurrentQuestion();

        String answer = currentQuestion.getAnswer();

        if (answer.equals(chatMessage.message())) {
            room.increasePlayerCorrectCount(sessionId);

            messageSender.sendBroadcast(
                    destination,
                    MessageType.QUESTION_RESULT,
                    toQuestionResultResponse(chatMessage.nickname(), answer));
            messageSender.sendBroadcast(destination, MessageType.RANK_UPDATE, toRankUpdateResponse(room));
            messageSender.sendBroadcast(
                    destination,
                    MessageType.SYSTEM_NOTICE,
                    ofPlayerEvent(chatMessage.nickname(), RoomEventType.CORRECT_ANSWER));

            timerService.cancelTimer(room);

            // TODO : 게임 종료 로직 추가
            if (!timerService.validateCurrentRound(room)) {
                // 게임 종료 로직
                return;
            }

            room.increaseCurrentRound();

            // 타이머 추가하기
            timerService.startTimer(room, CONTINUE_DELAY);
            messageSender.send(
                    destination,
                    MessageType.QUESTION_START,
                    toQuestionStartResponse(room, CONTINUE_DELAY));
        }
    }

    public void reconnectSession(
            Long roomId, String oldSessionId, String newSessionId, UserPrincipal principal) {
        Room room = findRoom(roomId);
        room.reconnectSession(oldSessionId, newSessionId);

        String destination = getDestination(roomId);

        messageSender.send(
                destination,
                MessageType.SYSTEM_NOTICE,
                ofPlayerEvent(principal.getUserNickname(), RoomEventType.RECONNECT));

        if (room.isPlaying()) {
            // todo 랭킹 리스트 추가
            messageSender.send(
                    destination, MessageType.GAME_START, toGameStartResponse(room.getQuestions()));
            messageSender.send(
                    destination,
                    MessageType.QUESTION_START,
                    toQuestionStartResponse(room, START_DELAY));
        } else {
            RoomSettingResponse roomSettingResponse = toRoomSettingResponse(room);

            Long quizId = room.getGameSetting().getQuizId();

            Quiz quiz = quizService.getQuizWithQuestionsById(quizId);

            GameSettingResponse gameSettingResponse =
                    toGameSettingResponse(room.getGameSetting(), quiz);

            PlayerListResponse playerListResponse = toPlayerListResponse(room);

            messageSender.send(destination, MessageType.ROOM_SETTING, roomSettingResponse);
            messageSender.send(destination, MessageType.GAME_SETTING, gameSettingResponse);
            messageSender.send(destination, MessageType.PLAYER_LIST, playerListResponse);
        }
    }

    public void changeConnectedStatus(Long roomId, String sessionId, ConnectionState newState) {
        Room room = findRoom(roomId);
        room.updatePlayerConnectionState(sessionId, newState);
    }

    public boolean isExit(String sessionId, Long roomId) {
        Room room = findRoom(roomId);
        return room.isExit(sessionId);
    }

    public void exitIfNotPlaying(Long roomId, String sessionId, UserPrincipal principal) {
        Room room = findRoom(roomId);
        if (!room.isPlaying()) {
            exitRoom(roomId, sessionId, principal);
        }
    }

    private Player getRemovePlayer(Room room, String sessionId, UserPrincipal principal) {
        Player removePlayer = room.getPlayerSessionMap().get(sessionId);
        if (removePlayer == null) {
            room.removeUserId(principal.getUserId());
            throw new CustomException(RoomErrorCode.SOCKET_SESSION_NOT_FOUND);
        }
        return removePlayer;
    }

    private Player createPlayer(UserPrincipal principal) {
        return new Player(principal.getUserId(), principal.getUserNickname());
    }

    private Player createPlayer() {
        return new Player(getCurrentUserId(), getCurrentUserNickname());
    }

    private Room findRoom(Long roomId) {
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

    private void changeHost(Room room, String hostSessionId) {
        Map<String, Player> playerSessionMap = room.getPlayerSessionMap();

        Optional<String> nextHostSessionId =
                playerSessionMap.entrySet().stream()
                        .filter(entry -> !entry.getKey().equals(hostSessionId))
                        .filter(entry -> entry.getValue().getState() == ConnectionState.CONNECTED)
                        .map(Map.Entry::getKey)
                        .findFirst();

        Player nextHost =
                playerSessionMap.get(
                        nextHostSessionId.orElseThrow(
                                () -> new CustomException(RoomErrorCode.SOCKET_SESSION_NOT_FOUND)));

        room.updateHost(nextHost);
        log.info("user_id:{} 방장 변경 완료 ", nextHost.getId());
    }

    private void removePlayer(Room room, String sessionId, Player removePlayer) {
        room.removeUserId(removePlayer.getId());
        room.removeSessionId(sessionId);
        room.removeValidatedUserId(removePlayer.getId());
    }

    private String getUserDestination() {
        return "/queue";
    }
}
