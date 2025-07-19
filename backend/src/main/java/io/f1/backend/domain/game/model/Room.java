package io.f1.backend.domain.game.model;

import io.f1.backend.domain.question.entity.Question;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
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

    private Map<Long, String> userIdSessionMap = new ConcurrentHashMap<>();

    private final LocalDateTime createdAt = LocalDateTime.now();

    private int currentRound = 0;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private ScheduledFuture<?> timer;

    public Room(Long id, RoomSetting roomSetting, GameSetting gameSetting, Player host) {
        this.id = id;
        this.roomSetting = roomSetting;
        this.gameSetting = gameSetting;
        this.host = host;
    }

    public boolean isHost(Long id) {
        return this.host.getId().equals(id);
    }

    public void updateQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public void updateHost(Player nextHost) {
        this.host = nextHost;
    }

    public void updateRoomState(RoomState newState) {
        this.state = newState;
    }

    public void removeUserId(Long id) {
        this.userIdSessionMap.remove(id);
    }

    public void removeSessionId(String sessionId) {
        this.playerSessionMap.remove(sessionId);
    }

    public void increasePlayerCorrectCount(String sessionId) {
        this.playerSessionMap.get(sessionId).increaseCorrectCount();
    }

    public Question getCurrentQuestion() {
        return questions.get(currentRound - 1);
    }

    public Boolean isPlaying() {
        return state == RoomState.PLAYING;
    }

    public void increaseCurrentRound() {
        currentRound++;
    }
}
