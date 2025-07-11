package io.f1.backend.domain.game.store;

import static org.assertj.core.api.Assertions.assertThat;

import io.f1.backend.domain.game.model.GameSetting;
import io.f1.backend.domain.game.model.Player;
import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.game.dto.request.RoomCreateRequest;
import io.f1.backend.domain.game.model.RoomSetting;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RoomRepositoryTests {

    RoomRepository roomRepository;

    @BeforeEach
    void setUp() {
        roomRepository = new RoomRepository();
    }

    @Test
    @DisplayName("게임 방 생성 테스트")
    void saveRoom_test() {

        RoomCreateRequest request = new RoomCreateRequest("방제", 3, "password1", true);
        Map<String, Object> loginUser = new HashMap<>();

        loginUser.put("id", 1L);
        loginUser.put("nickname", "빵야빵야");


        GameSetting gameSetting = new GameSetting(1L, 10, 60);

        Player host = new Player((Long) loginUser.get("id"), loginUser.get("nickname").toString());

        RoomSetting roomSetting = new RoomSetting(request.roomName(), request.maxUserCount(),
            request.locked(), request.password());

        Long savedId = roomRepository.saveRoom(gameSetting,host,roomSetting);

        Room savedRoom = roomRepository.getRoomForTest(savedId);

        assertThat(savedRoom.getHost().getId()).isEqualTo(loginUser.get("id"));
        assertThat(savedRoom.getHost().getNickname()).isEqualTo(loginUser.get("nickname"));

        assertThat(savedRoom.getRoomSetting().roomName()).isEqualTo(request.roomName());
        assertThat(savedRoom.getRoomSetting().maxUserCount()).isEqualTo(request.maxUserCount());
        assertThat(savedRoom.getRoomSetting().locked()).isEqualTo(request.locked());
        assertThat(savedRoom.getRoomSetting().password()).isEqualTo(request.password());
    }

}