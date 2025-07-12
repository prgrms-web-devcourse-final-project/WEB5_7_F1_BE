package io.f1.backend.domain.game.store;

import io.f1.backend.domain.game.model.Room;
import java.util.List;

public interface RoomRepository {

    void saveRoom(Room room);

    List<Room> findAll();

}
