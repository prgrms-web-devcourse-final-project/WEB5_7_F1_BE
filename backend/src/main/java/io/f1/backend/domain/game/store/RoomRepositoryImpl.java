package io.f1.backend.domain.game.store;

import io.f1.backend.domain.game.model.Room;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class RoomRepositoryImpl implements RoomRepository {

    private final Map<Long, Room> roomMap = new ConcurrentHashMap<>();

    @Override
    public void saveRoom(Room room) {
        roomMap.put(room.getId(), room);
    }

    //테스트 전용 메소드
    public Room getRoomForTest(Long roomId) {
        return roomMap.get(roomId);
    }

}