package io.f1.backend.domain.game.app;

import static io.f1.backend.domain.game.mapper.RoomMapper.*;

import io.f1.backend.domain.game.dto.RoomInitialData;
import io.f1.backend.domain.game.dto.request.RoomCreateRequest;
import io.f1.backend.domain.game.dto.request.RoomValidationRequest;
import io.f1.backend.domain.game.dto.response.GameSettingResponse;
import io.f1.backend.domain.game.dto.response.PlayerListResponse;
import io.f1.backend.domain.game.dto.response.QuizResponse;
import io.f1.backend.domain.game.dto.response.RoomCreateResponse;
import io.f1.backend.domain.game.dto.response.RoomListResponse;
import io.f1.backend.domain.game.dto.response.RoomResponse;
import io.f1.backend.domain.game.dto.response.RoomSettingResponse;
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
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final QuizService quizService;
    private final RoomRepository roomRepository;
    private final AtomicLong roomIdGenerator = new AtomicLong(0);
    private final ApplicationEventPublisher eventPublisher;

    public RoomCreateResponse saveRoom(RoomCreateRequest request, Map<String, Object> loginUser) {

        // todo 제일 작은 index quizId 가져와서 gameSetting(round 설정)
        GameSetting gameSetting = new GameSetting(1L, 10, 60);
        // todo security에서 가져오는걸로 변경
        Player host = new Player((Long) loginUser.get("id"), loginUser.get("nickname").toString());
        RoomSetting roomSetting = toRoomSetting(request);

        Long newId = roomIdGenerator.incrementAndGet();

        Room room = new Room(newId, roomSetting, gameSetting, host);

        roomRepository.saveRoom(room);

        Long quizId = room.getGameSetting().getQuizId();
        Quiz quiz = quizService.getQuizById(quizId);

        eventPublisher.publishEvent(new RoomCreatedEvent(room, quiz));

        return new RoomCreateResponse(newId);
    }

    public void validateRoom(RoomValidationRequest request) {

        Room room =
                roomRepository
                        .findRoom(request.roomId())
                        .orElseThrow(() -> new IllegalArgumentException("404 존재하지 않는 방입니다."));

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

        // todo security
        Player player = new Player(1L, "빵야빵야");

        Map<String, Player> playerSessionMap = room.getPlayerSessionMap();

        playerSessionMap.put(sessionId, player);

        String destination = "/sub/room/" + roomId;

        RoomSettingResponse roomSettingResponse = toRoomSettingResponse(room);
        // todo quiz 생성 api 완성 후 수정
        QuizResponse quiz =
                new QuizResponse(room.getGameSetting().getQuizId(), "title", "설명", "url", 10);
        GameSettingResponse gameSettingResponse =
                toGameSettingResponse(room.getGameSetting(), quiz);

        PlayerListResponse playerListResponse = toPlayerListResponse(room);

        return new RoomInitialData(
                destination, roomSettingResponse, gameSettingResponse, playerListResponse);
    }

    public RoomListResponse getAllRooms() {
        List<Room> rooms = roomRepository.findAll();
        List<RoomResponse> roomResponses = rooms.stream()
            .map(room -> {
                Long quizId = room.getGameSetting().getQuizId();
                Quiz quiz = quizService.getQuizById(quizId);

                return toRoomResponse(room, quiz);
                })
            .toList();
        return new RoomListResponse(roomResponses);
    }
}
