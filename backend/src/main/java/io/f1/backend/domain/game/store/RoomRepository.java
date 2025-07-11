package io.f1.backend.domain.game.store;

import io.f1.backend.domain.game.model.GameSetting;
import io.f1.backend.domain.game.model.Player;
import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.game.model.RoomSetting;
import io.f1.backend.domain.game.dto.request.RoomCreateRequest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class RoomRepository {

    private final Map<Long, Room> rooms = new ConcurrentHashMap<>();
    private final AtomicLong roomIdGenerator = new AtomicLong(0);

    public Long saveRoom(RoomCreateRequest request, Map<String, Object> loginUser) {
        Long newId = roomIdGenerator.incrementAndGet();

        RoomSetting roomSetting = new RoomSetting(request.roomName(), request.maxUserCount(),
            request.locked(), request.password());

        //todo 제일 작은 index quizId 가져와서 gameSetting
        GameSetting gameSetting = new GameSetting(1L, 10, 60);

        Player host = new Player((Long) loginUser.get("id"), loginUser.get("nickname").toString());

        Room newRoom = new Room(newId, roomSetting, gameSetting, host);

        rooms.put(newId, newRoom);

        return newId;
    }

    //테스트 전용 메소드
    public Room getRoomForTest(Long roomId) {
        return rooms.get(roomId);
    }

}