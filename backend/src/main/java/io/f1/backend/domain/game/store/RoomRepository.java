package io.f1.backend.domain.game.store;

import io.f1.backend.domain.game.model.Room;

public interface RoomRepository {
    void saveRoom(Room room);
}
