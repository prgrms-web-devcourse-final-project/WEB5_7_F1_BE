package io.f1.backend.domain.game.store;

import io.f1.backend.domain.game.model.GameSetting;
import io.f1.backend.domain.game.model.Player;
import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.game.model.RoomSetting;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class RoomRepository {

    private final Map<Long, Room> rooms = new ConcurrentHashMap<>();
    private final AtomicLong roomIdGenerator = new AtomicLong(0);

    public Long saveRoom(GameSetting gameSetting, Player host, RoomSetting roomSetting) {
        Long newId = roomIdGenerator.incrementAndGet();

        rooms.put(newId, new Room(newId, roomSetting, gameSetting, host));

        return newId;
    }

    // 테스트 전용 메소드
    public Room getRoomForTest(Long roomId) {
        return rooms.get(roomId);
    }
}
