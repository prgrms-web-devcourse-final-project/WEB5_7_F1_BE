package io.f1.backend.domain.game.store;

import io.f1.backend.domain.game.model.Player;
import io.f1.backend.domain.game.model.Room;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class UserRoomRepository {

    private final Map<Long, Long> userRoomMap = new ConcurrentHashMap<>();

    public void addUser(Player player, Room room) {
        userRoomMap.put(player.getId(), room.getId());
    }

    public Long getRoomId(Long userId) {
        return userRoomMap.get(userId);
    }

    public void removeUser(Long userId, Long roomId) {
        userRoomMap.remove(userId, roomId);
    }

    public boolean isUserInAnyRoom(Long userId) {
        return userRoomMap.containsKey(userId);
    }

}
