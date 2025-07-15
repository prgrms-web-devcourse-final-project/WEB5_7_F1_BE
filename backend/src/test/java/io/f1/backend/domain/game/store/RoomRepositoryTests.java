package io.f1.backend.domain.game.store;

import static org.assertj.core.api.Assertions.assertThat;

import io.f1.backend.domain.game.dto.request.RoomCreateRequest;
import io.f1.backend.domain.game.model.GameSetting;
import io.f1.backend.domain.game.model.Player;
import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.game.model.RoomSetting;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class RoomRepositoryTests {

    RoomRepositoryImpl roomRepository;

    @BeforeEach
    void setUp() {
        roomRepository = new RoomRepositoryImpl();
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

        RoomSetting roomSetting =
                new RoomSetting(
                        request.roomName(),
                        request.maxUserCount(),
                        request.locked(),
                        request.password());

        Long newId = 1L;

        Room newRoom = new Room(newId, roomSetting, gameSetting, host);

        roomRepository.saveRoom(newRoom);

        Room savedRoom = roomRepository.getRoomForTest(newId);

        assertThat(savedRoom.getHost().getId()).isEqualTo(loginUser.get("id"));
        assertThat(savedRoom.getHost().getNickname()).isEqualTo(loginUser.get("nickname"));

        assertThat(savedRoom.getRoomSetting().roomName()).isEqualTo(request.roomName());
        assertThat(savedRoom.getRoomSetting().maxUserCount()).isEqualTo(request.maxUserCount());
        assertThat(savedRoom.getRoomSetting().locked()).isEqualTo(request.locked());
        assertThat(savedRoom.getRoomSetting().password()).isEqualTo(request.password());
    }

    @Test
    @DisplayName("게임 방 전체 조회 테스트")
    void findAll_test() throws Exception {

        // given: 테스트를 위한 방 2개 생성 및 저장
        RoomCreateRequest request1 = new RoomCreateRequest("방이름_1", 3, "password1", true);
        RoomCreateRequest request2 = new RoomCreateRequest("방이름_2", 5, "", false);

        Player host1 = new Player(1L, "방장 1");
        Player host2 = new Player(2L, "호스트2");

        GameSetting gameSetting = new GameSetting(1L, 10, 60);

        RoomSetting roomSetting1 =
                new RoomSetting(
                        request1.roomName(),
                        request1.maxUserCount(),
                        request1.locked(),
                        request1.password());
        RoomSetting roomSetting2 =
                new RoomSetting(
                        request2.roomName(),
                        request2.maxUserCount(),
                        request2.locked(),
                        request2.password());

        Room room1 = new Room(1L, roomSetting1, gameSetting, host1);
        Room room2 = new Room(2L, roomSetting2, gameSetting, host2);

        roomRepository.saveRoom(room1);
        roomRepository.saveRoom(room2);

        // when: 방 전체 조회 메서드로 전체 방 리스트 조회
        List<Room> allRooms = roomRepository.findAll();

        // then: 저장한 방 2개가 모두 조회되어야하고
        assertThat(allRooms).hasSize(2);
        assertThat(allRooms).extracting("id").containsExactlyInAnyOrder(1L, 2L);
        assertThat(allRooms)
                .extracting(room -> room.getRoomSetting().roomName())
                .containsExactlyInAnyOrder("방이름_1", "방이름_2");
    }
}
