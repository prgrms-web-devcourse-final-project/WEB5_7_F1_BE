package io.f1.backend.domain.game.store;

import io.f1.backend.domain.game.model.Player;
import io.f1.backend.domain.game.model.Room;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class RoomRepositoryImpl implements RoomRepository {

    private final Map<Long, Room> roomMap = new ConcurrentHashMap<>();

    @Override
    public void saveRoom(Room room) {
        roomMap.put(room.getId(), room);
    }

    @Override
    public Optional<Room> findRoom(Long roomId) {
        return Optional.ofNullable(roomMap.get(roomId));
    }

    @Override
    public List<Room> findAll() {
        return new ArrayList<>(roomMap.values());
    }

    @Override
    public void removeRoom(Long roomId) {
        roomMap.remove(roomId);
    }

    @Override
    public Optional<Player> findPlayerInRoomBySessionId(Long roomId, String sessionId) {
        return findRoom(roomId).map(room -> room.getPlayerSessionMap().get(sessionId));
    }

    // 테스트 전용 메소드
    public Room getRoomForTest(Long roomId) {
        return roomMap.get(roomId);
    }
}
