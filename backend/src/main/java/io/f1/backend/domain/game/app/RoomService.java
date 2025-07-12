package io.f1.backend.domain.game.app;

import static io.f1.backend.domain.game.mapper.RoomMapper.toRoomSetting;

import io.f1.backend.domain.game.dto.request.RoomCreateRequest;
import io.f1.backend.domain.game.dto.response.RoomCreateResponse;
import io.f1.backend.domain.game.model.GameSetting;
import io.f1.backend.domain.game.model.Player;
import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.game.model.RoomSetting;
import io.f1.backend.domain.game.store.RoomRepository;

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
}
