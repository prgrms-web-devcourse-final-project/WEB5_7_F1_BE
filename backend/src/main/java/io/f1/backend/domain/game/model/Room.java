package io.f1.backend.domain.game.model;

import io.f1.backend.domain.question.entity.Question;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class Room {

    private final Long id;

    private final RoomSetting roomSetting;

    private GameSetting gameSetting;

    private RoomState state = RoomState.WAITING;

    private Player host;

    private List<Question> questions = new ArrayList<>();

    private Map<String, Player> playerSessionMap = new ConcurrentHashMap<>();

    private final LocalDateTime createdAt = LocalDateTime.now();

    public Room(Long id, RoomSetting roomSetting, GameSetting gameSetting, Player host) {
        this.id = id;
        this.roomSetting = roomSetting;
        this.gameSetting = gameSetting;
        this.host = host;
    }
}
