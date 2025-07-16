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

import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final QuizService quizService;
    private final RoomRepository roomRepository;
    private final AtomicLong roomIdGenerator = new AtomicLong(0);
    private final ApplicationEventPublisher eventPublisher;

    public RoomCreateResponse saveRoom(RoomCreateRequest request) {

        Long quizMinId = quizService.getQuizMinId();
        Quiz quiz = quizService.getQuizById(quizMinId);

        GameSetting gameSetting = toGameSetting(quiz);

        Player host = createPlayer();

        RoomSetting roomSetting = toRoomSetting(request);

        Long newId = roomIdGenerator.incrementAndGet();

        Room room = new Room(newId, roomSetting, gameSetting, host);

        roomRepository.saveRoom(room);

        eventPublisher.publishEvent(new RoomCreatedEvent(room, quiz));

        return new RoomCreateResponse(newId);
    }

    public void validateRoom(RoomValidationRequest request) {

        Room room =
                roomRepository
                        .findRoom(request.roomId())
                        .orElseThrow(() -> new IllegalArgumentException("404 존재하지 않는 방입니다.-1"));

        if (room.getState().equals(RoomState.PLAYING)) {
            throw new IllegalArgumentException("403 게임이 진행중입니다.");
        }

        int maxUserCnt = room.getRoomSetting().maxUserCount();
        int currentCnt = room.getPlayerSessionMap().size();
        if (maxUserCnt == currentCnt) {
            throw new IllegalArgumentException("403 정원이 모두 찼습니다.");
        }

        if (room.getRoomSetting().locked()
                && !room.getRoomSetting().password().equals(request.password())) {
            throw new IllegalArgumentException("401 비밀번호가 일치하지 않습니다.");
        }
    }

    public RoomInitialData enterRoom(Long roomId, String sessionId) {

        Room room =
                roomRepository
                        .findRoom(roomId)
                        .orElseThrow(() -> new IllegalArgumentException("404 존재하지 않는 방입니다."));

        Player player = createPlayer();

        Map<String, Player> playerSessionMap = room.getPlayerSessionMap();

        playerSessionMap.put(sessionId, player);

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
        Room room =
                roomRepository
                        .findRoom(roomId)
                        .orElseThrow(() -> new IllegalArgumentException("404 존재하지 않는 방입니다."));

        Map<String, Player> playerSessionMap = room.getPlayerSessionMap();

        String destination = getDestination(roomId);

        if (playerSessionMap.size() == 1 && playerSessionMap.get(sessionId) != null) {
            roomRepository.removeRoom(roomId);
            return RoomExitData.builder().destination(destination).removedRoom(true).build();
        }

        Player removedPlayer = playerSessionMap.remove(sessionId);
        if (removedPlayer == null) {
            throw new IllegalArgumentException("퇴장 처리 불가 - 404 해당 세션 플레이어는 존재하지않습니다.");
        }

        if (room.getHost().getId().equals(removedPlayer.getId())) {
            Optional<String> nextHostSessionId = playerSessionMap.keySet().stream().findFirst();
            Player nextHost =
                    playerSessionMap.get(
                            nextHostSessionId.orElseThrow(
                                    () ->
                                            new IllegalArgumentException(
                                                    "방장 교체 불가 - 404 해당 세션 플레이어는 존재하지않습니다.")));
            room.updateHost(nextHost);
        }

        SystemNoticeResponse systemNoticeResponse =
                ofPlayerEvent(removedPlayer, RoomEventType.EXIT);

        PlayerListResponse playerListResponse = toPlayerListResponse(room);

        return new RoomExitData(destination, playerListResponse, systemNoticeResponse, false);
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

    private static String getDestination(Long roomId) {
        return "/sub/room/" + roomId;
    }

    private static Player createPlayer() {
        return new Player(getCurrentUserId(), getCurrentUserNickname());
    }

    @Transactional(readOnly = true)
    public Integer checkGameSetting(Long roomId, Long quizId) {
        Room room =
            roomRepository
                .findRoom(roomId)
                .orElseThrow(() -> new IllegalArgumentException("404 존재하지 않는 방입니다."));

        GameSetting gameSetting = room.getGameSetting();

        Long roomQuizId = gameSetting.getQuizId();

        // TODO : 에러 코드 추가하기
        if(!roomQuizId.equals(quizId)) {
            throw new IllegalArgumentException("게임 설정이 다릅니다. (게임을 시작할 수 없습니다.)");
        }

        return gameSetting.getRound();
    }

    @Transactional
    public void gameStart(Long roomId) {
        Room room =
            roomRepository
                .findRoom(roomId)
                .orElseThrow(() -> new IllegalArgumentException("404 존재하지 않는 방입니다."));

        room.gameStart();
    }
}
