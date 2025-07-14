package io.f1.backend.domain.game.store;

import io.f1.backend.domain.game.model.Room;

import java.util.List;
import java.util.Optional;

public interface RoomRepository {
    void saveRoom(Room room);

    Optional<Room> findRoom(Long roomId);

    List<Room> findAll();
}
