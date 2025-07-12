package io.f1.backend.domain.game.app;

import static io.f1.backend.domain.game.mapper.RoomMapper.toRoomSetting;

import io.f1.backend.domain.game.dto.response.RoomListResponse;
import io.f1.backend.domain.game.dto.request.RoomCreateRequest;
import io.f1.backend.domain.game.dto.response.RoomCreateResponse;
import io.f1.backend.domain.game.dto.response.RoomResponse;
import io.f1.backend.domain.game.mapper.RoomMapper;
import io.f1.backend.domain.game.model.GameSetting;
import io.f1.backend.domain.game.model.Player;
import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.game.model.RoomSetting;
import io.f1.backend.domain.game.store.RoomRepository;

import io.f1.backend.domain.quiz.entity.Quiz;
import io.f1.backend.domain.user.entity.User;
import java.util.List;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final AtomicLong roomIdGenerator = new AtomicLong(0);

    public RoomCreateResponse saveRoom(RoomCreateRequest request, Map<String, Object> loginUser) {

        // todo 제일 작은 index quizId 가져와서 gameSetting(round 설정)
        GameSetting gameSetting = new GameSetting(1L, 10, 60);
        // todo security에서 가져오는걸로 변경
        Player host = new Player((Long) loginUser.get("id"), loginUser.get("nickname").toString());
        RoomSetting roomSetting = toRoomSetting(request);

        Long newId = roomIdGenerator.incrementAndGet();

        roomRepository.saveRoom(new Room(newId, roomSetting, gameSetting, host));

        return new RoomCreateResponse(newId);
    }

    // todo quizService에서 퀴즈 조회 메서드로 변경
    public RoomListResponse getAllRooms() {
        List<Room> rooms = roomRepository.findAll();
        List<RoomResponse> roomResponses = rooms.stream().map(room -> {

            User user = new User(); // 임시 유저 객체
            user.setNickname("임시 유저 닉네임");

            Quiz quiz = new Quiz(); // 임시 퀴즈 객체
            quiz.setTitle("임시 퀴즈 제목");
            quiz.setDescription("임시 퀴즈 설명");
            quiz.setThumbnailUrl("임시 이미지");
            quiz.setQuestions(List.of());
            quiz.setCreator(user);

            return RoomMapper.toRoomResponse(room, quiz);
        }).toList();
        return new RoomListResponse(roomResponses);
    }
}
