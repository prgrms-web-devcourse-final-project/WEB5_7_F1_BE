package io.f1.backend.domain.game.app;

import static io.f1.backend.domain.game.mapper.RoomMapper.ofPlayerEvent;
import static io.f1.backend.domain.game.mapper.RoomMapper.toGameSetting;
import static io.f1.backend.domain.game.mapper.RoomMapper.toGameSettingResponse;
import static io.f1.backend.domain.game.mapper.RoomMapper.toPlayerListResponse;
import static io.f1.backend.domain.game.mapper.RoomMapper.toQuestionResultResponse;
import static io.f1.backend.domain.game.mapper.RoomMapper.toRankUpdateResponse;
import static io.f1.backend.domain.game.mapper.RoomMapper.toRoomResponse;
import static io.f1.backend.domain.game.mapper.RoomMapper.toRoomSetting;
import static io.f1.backend.domain.game.mapper.RoomMapper.toRoomSettingResponse;
import static io.f1.backend.global.util.SecurityUtils.getCurrentUserId;
import static io.f1.backend.global.util.SecurityUtils.getCurrentUserNickname;

import io.f1.backend.domain.game.dto.ChatMessage;
import io.f1.backend.domain.game.dto.RoomEventType;
import io.f1.backend.domain.game.dto.RoomExitData;
import io.f1.backend.domain.game.dto.RoomInitialData;
import io.f1.backend.domain.game.dto.RoundResult;
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
import io.f1.backend.domain.game.model.GameSetting;
import io.f1.backend.domain.game.model.Player;
import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.game.model.RoomSetting;
import io.f1.backend.domain.game.model.RoomState;
import io.f1.backend.domain.game.store.RoomRepository;
import io.f1.backend.domain.question.entity.Question;
import io.f1.backend.domain.quiz.app.QuizService;
import io.f1.backend.domain.quiz.entity.Quiz;
import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.exception.errorcode.QuestionErrorCode;
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

    private final QuizService quizService;
    private final RoomRepository roomRepository;
    private final AtomicLong roomIdGenerator = new AtomicLong(0);
    private final ApplicationEventPublisher eventPublisher;
    private final Map<Long, Object> roomLocks = new ConcurrentHashMap<>();
    private static final String PENDING_SESSION_ID = "PENDING_SESSION_ID";

    public RoomCreateResponse saveRoom(RoomCreateRequest request) {

        Long quizMinId = quizService.getQuizMinId();
        Quiz quiz = quizService.getQuizWithQuestionsById(quizMinId);

        GameSetting gameSetting = toGameSetting(quiz);

        Player host = createPlayer();

        RoomSetting roomSetting = toRoomSetting(request);

        Long newId = roomIdGenerator.incrementAndGet();

        Room room = new Room(newId, roomSetting, gameSetting, host);

        room.getUserIdSessionMap().put(host.id, PENDING_SESSION_ID);

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
            int currentCnt = room.getUserIdSessionMap().size();
            if (maxUserCnt == currentCnt) {
                throw new CustomException(RoomErrorCode.ROOM_USER_LIMIT_REACHED);
            }

            if (room.getRoomSetting().locked()
                    && !room.getRoomSetting().password().equals(request.password())) {
                throw new CustomException(RoomErrorCode.WRONG_PASSWORD);
            }

            room.getUserIdSessionMap().put(getCurrentUserId(), PENDING_SESSION_ID);
        }
    }

    public RoomInitialData initializeRoomSocket(Long roomId, String sessionId) {

        Room room = findRoom(roomId);

        Player player = createPlayer();

        Map<String, Player> playerSessionMap = room.getPlayerSessionMap();
        Map<Long, String> userIdSessionMap = room.getUserIdSessionMap();

        if (room.isHost(player.getId())) {
            player.toggleReady();
        }

        playerSessionMap.put(sessionId, player);
        String existingSession = userIdSessionMap.get(player.getId());
        /* 정상 흐름 or 재연결 */
        if (existingSession.equals(PENDING_SESSION_ID) || !existingSession.equals(sessionId)) {
            userIdSessionMap.put(player.getId(), sessionId);
        }

        RoomSettingResponse roomSettingResponse = toRoomSettingResponse(room);

        Long quizId = room.getGameSetting().getQuizId();
        Quiz quiz = quizService.getQuizWithQuestionsById(quizId);

        GameSettingResponse gameSettingResponse =
                toGameSettingResponse(room.getGameSetting(), quiz);

        PlayerListResponse playerListResponse = toPlayerListResponse(room);

        SystemNoticeResponse systemNoticeResponse =
                ofPlayerEvent(player.getNickname(), RoomEventType.ENTER);

        return new RoomInitialData(
                getDestination(roomId),
                roomSettingResponse,
                gameSettingResponse,
                playerListResponse,
                systemNoticeResponse);
    }

    public RoomExitData exitRoom(Long roomId, String sessionId) {

        Object lock = roomLocks.computeIfAbsent(roomId, k -> new Object());

        synchronized (lock) {
            Room room = findRoom(roomId);

            String destination = getDestination(roomId);

            Player removePlayer = getRemovePlayer(room, sessionId);

            /* 방 삭제 */
            if (isLastPlayer(room, sessionId)) {
                return removeRoom(room, destination);
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

            return new RoomExitData(destination, playerListResponse, systemNoticeResponse, false);
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

    public RoundResult chat(Long roomId, String sessionId, ChatMessage chatMessage) {
        Room room = findRoom(roomId);

        String destination = getDestination(roomId);

        if(!room.isPlaying()) {
            return buildResultOnlyChat(destination, chatMessage);
        }

        Long questionId = room.getCurrentQuestionId();

        Question question =
                room.getQuestions().stream()
                        .filter(q -> q.getId().equals(questionId))
                        .findFirst()
                        .orElseThrow(
                                () -> new CustomException(QuestionErrorCode.QUESTION_NOT_FOUND));

        String answer = question.getAnswer();

        if (!answer.equals(chatMessage.message())) {
            return buildResultOnlyChat(destination, chatMessage);
        }

        room.increasePlayerCorrectCount(sessionId);

        return RoundResult.builder()
                .destination(destination)
                .questionResult(toQuestionResultResponse(questionId,chatMessage, answer))
                .rankUpdate(toRankUpdateResponse(room))
                .systemNotice(ofPlayerEvent(chatMessage.nickname(), RoomEventType.ENTER))
                .chat(chatMessage)
                .build();
    }

    private Player getRemovePlayer(Room room, String sessionId) {
        Player removePlayer = room.getPlayerSessionMap().get(sessionId);
        if (removePlayer == null) {
            room.removeUserId(getCurrentUserId());
            throw new CustomException(RoomErrorCode.SOCKET_SESSION_NOT_FOUND);
        }
        return removePlayer;
    }

    private static String getDestination(Long roomId) {
        return "/sub/room/" + roomId;
    }

    private Player createPlayer() {
        return new Player(getCurrentUserId(), getCurrentUserNickname());
    }

    private Room findRoom(Long roomId) {
        return roomRepository
                .findRoom(roomId)
                .orElseThrow(() -> new CustomException(RoomErrorCode.ROOM_NOT_FOUND));
    }

    private boolean isLastPlayer(Room room, String sessionId) {
        Map<String, Player> playerSessionMap = room.getPlayerSessionMap();
        return playerSessionMap.size() == 1 && playerSessionMap.containsKey(sessionId);
    }

    private RoomExitData removeRoom(Room room, String destination) {
        Long roomId = room.getId();
        roomRepository.removeRoom(roomId);
        roomLocks.remove(roomId);
        log.info("{}번 방 삭제", roomId);
        return RoomExitData.builder().destination(destination).removedRoom(true).build();
    }

    private void changeHost(Room room, String hostSessionId) {
        Map<String, Player> playerSessionMap = room.getPlayerSessionMap();

        Optional<String> nextHostSessionId =
                playerSessionMap.keySet().stream()
                        .filter(key -> !key.equals(hostSessionId))
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
    }

    private RoundResult buildResultOnlyChat(String destination, ChatMessage chatMessage) {
        return RoundResult.builder()
            .destination(destination)
            .chat(chatMessage)
            .build();
    }
}
