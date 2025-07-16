package io.f1.backend.domain.game.app;

import static io.f1.backend.domain.game.mapper.RoomMapper.ofPlayerEvent;
import static io.f1.backend.domain.game.mapper.RoomMapper.toGameSetting;
import static io.f1.backend.domain.game.mapper.RoomMapper.toGameSettingResponse;
import static io.f1.backend.domain.game.mapper.RoomMapper.toPlayerListResponse;
import static io.f1.backend.domain.game.mapper.RoomMapper.toRoomResponse;
import static io.f1.backend.domain.game.mapper.RoomMapper.toRoomSetting;
import static io.f1.backend.domain.game.mapper.RoomMapper.toRoomSettingResponse;
import static io.f1.backend.global.util.SecurityUtils.getCurrentUserId;
import static io.f1.backend.global.util.SecurityUtils.getCurrentUserNickname;

import io.f1.backend.domain.game.dto.RoomEventType;
import io.f1.backend.domain.game.dto.RoomExitData;
import io.f1.backend.domain.game.dto.RoomInitialData;
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
import io.f1.backend.domain.quiz.app.QuizService;
import io.f1.backend.domain.quiz.entity.Quiz;
import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.exception.errorcode.RoomErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.hibernate.boot.model.naming.IllegalIdentifierException;
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
    private final AtomicLong roomIdGenerator = new AtomicLong(0);
    private final ApplicationEventPublisher eventPublisher;
    private final Map<Long, Object> roomLocks = new ConcurrentHashMap<>();
    private static final String PENDING_SESSION_ID = "PENDING_SESSION_ID";

    public RoomCreateResponse saveRoom(RoomCreateRequest request) {

        Long quizMinId = quizService.getQuizMinId();
        Quiz quiz = quizService.getQuizById(quizMinId);

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
                throw new IllegalArgumentException("403 게임이 진행중입니다.");
            }

            int maxUserCnt = room.getRoomSetting().maxUserCount();
            int currentCnt = room.getUserIdSessionMap().size();
            if (maxUserCnt == currentCnt) {
                throw new IllegalArgumentException("403 정원이 모두 찼습니다.");
            }

            if (room.getRoomSetting().locked()
                    && !room.getRoomSetting().password().equals(request.password())) {
                throw new IllegalArgumentException("401 비밀번호가 일치하지 않습니다.");
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
        Quiz quiz = quizService.getQuizById(quizId);

        GameSettingResponse gameSettingResponse =
                toGameSettingResponse(room.getGameSetting(), quiz);

        PlayerListResponse playerListResponse = toPlayerListResponse(room);

        SystemNoticeResponse systemNoticeResponse = ofPlayerEvent(player, RoomEventType.ENTER);

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
                    ofPlayerEvent(removePlayer, RoomEventType.EXIT);

            PlayerListResponse playerListResponse = toPlayerListResponse(room);

            return new RoomExitData(destination, playerListResponse, systemNoticeResponse, false);
        }
    }

    public Player findPlayerInRoomBySessionId(String sessionId, Long roomId) {
        return roomRepository
                .findPlayerInRoomBySessionId(sessionId, roomId)
                .orElseThrow(() -> new CustomException(RoomErrorCode.PLAYER_NOT_FOUND));
    }

    public RoomListResponse getAllRooms() {
        List<Room> rooms = roomRepository.findAll();
        List<RoomResponse> roomResponses =
                rooms.stream()
                        .map(
                                room -> {
                                    Long quizId = room.getGameSetting().getQuizId();
                                    Quiz quiz = quizService.getQuizById(quizId);

                                    return toRoomResponse(room, quiz);
                                })
                        .toList();
        return new RoomListResponse(roomResponses);
    }

    private Player getRemovePlayer(Room room, String sessionId) {
        Player removePlayer = room.getPlayerSessionMap().get(sessionId);
        if (removePlayer == null) {
            room.removeUserId(getCurrentUserId());
            throw new IllegalIdentifierException("404 세션 없음 비정상적인 퇴장 요청");
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
                .orElseThrow(() -> new IllegalArgumentException("404 존재하지 않는 방입니다."));
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
                                () ->
                                        new IllegalArgumentException(
                                                "방장 교체 불가 - 404 해당 세션 플레이어는 존재하지않습니다.")));

        room.updateHost(nextHost);
        log.info("user_id:{} 방장 변경 완료 ", nextHost.getId());
    }

    private void removePlayer(Room room, String sessionId, Player removePlayer) {
        room.removeUserId(removePlayer.getId());
        room.removeSessionId(sessionId);
    }

    public PlayerListResponse getPlayerListResponse(Long roomId) {
        Room room =
                roomRepository
                        .findRoom(roomId)
                        .orElseThrow(
                                () -> new IllegalStateException("방을 찾을 수 없습니다. roomId=" + roomId));

        return toPlayerListResponse(room);
    }
}
