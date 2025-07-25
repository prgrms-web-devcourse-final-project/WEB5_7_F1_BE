package io.f1.backend.domain.game.model;

import io.f1.backend.domain.game.dto.request.TimeLimit;
import io.f1.backend.domain.question.entity.Question;
import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.exception.errorcode.RoomErrorCode;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

@Getter
public class Room {

    private final Long id;

    private final RoomSetting roomSetting;

    private GameSetting gameSetting;

    private RoomState state = RoomState.WAITING;

    private Player host;

    private List<Question> questions = new ArrayList<>();

    private Map<String, Player> playerSessionMap = new ConcurrentHashMap<>();

    private final Set<Long> validatedUserIds = new HashSet<>();

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

    public void addValidatedUserId(Long userId) {
        validatedUserIds.add(userId);
    }

    public int getCurrentUserCnt() {
        return validatedUserIds.size();
    }

    public void addPlayer(String sessionId, Player player) {
        Long userId = player.getId();
        if (!validatedUserIds.contains(userId)) {
            throw new CustomException(RoomErrorCode.ROOM_ENTER_REQUIRED);
        }

        if (isHost(userId)) {
            player.toggleReady();
        }
        playerSessionMap.put(sessionId, player);
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

    public void updateTimer(ScheduledFuture<?> timer) {
        this.timer = timer;
    }

    public void removeSessionId(String sessionId) {
        this.playerSessionMap.remove(sessionId);
    }

    public void removeValidatedUserId(Long userId) {
        validatedUserIds.remove(userId);
    }

    public void increasePlayerCorrectCount(String sessionId) {
        this.playerSessionMap.get(sessionId).increaseCorrectCount();
    }

    public Question getCurrentQuestion() {
        return questions.get(currentRound - 1);
    }

    public boolean isPlaying() {
        return state == RoomState.PLAYING;
    }

    public void increaseCurrentRound() {
        currentRound++;
    }

    public void initializeRound() {
        currentRound = 0;
    }

    public List<Player> getDisconnectedPlayers() {
        List<Player> disconnectedPlayers = new ArrayList<>();

        for (Player player : this.playerSessionMap.values()) {
            if (player.getState().equals(ConnectionState.DISCONNECTED)) {
                disconnectedPlayers.add(player);
            }
        }
        return disconnectedPlayers;
    }

    public void initializePlayers() {
        this.playerSessionMap
                .values()
                .forEach(
                        player -> {
                            player.initializeCorrectCount();
                            player.toggleReady();
                        });
    }

    public String getSessionIdByUserId(Long userId) {
        for (Map.Entry<String, Player> entry : playerSessionMap.entrySet()) {
            if (entry.getValue().getId().equals(userId)) {
                return entry.getKey();
            }
        }
        throw new CustomException(RoomErrorCode.PLAYER_NOT_FOUND);
    }

    public void reconnectSession(String oldSessionId, String newSessionId) {
        Player player = playerSessionMap.get(oldSessionId);
        removeSessionId(oldSessionId);
        player.updateState(ConnectionState.CONNECTED);
        playerSessionMap.put(newSessionId, player);
    }

    public void updatePlayerConnectionState(String sessionId, ConnectionState newState) {
        playerSessionMap.get(sessionId).updateState(newState);
    }

    public boolean isExit(String sessionId) {
        return playerSessionMap.get(sessionId) == null;
    }

    public boolean isLastPlayer(String sessionId) {
        long connectedCount =
                playerSessionMap.values().stream()
                        .filter(player -> player.getState() == ConnectionState.CONNECTED)
                        .count();
        return connectedCount == 1 && playerSessionMap.containsKey(sessionId);
    }

    public boolean validateReadyStatus() {

        return playerSessionMap.values().stream().allMatch(Player::isReady);
    }

    public Player getPlayerBySessionId(String sessionId) {
        Player player = playerSessionMap.get(sessionId);
        if (player == null) {
            throw new CustomException(RoomErrorCode.PLAYER_NOT_FOUND);
        }
        return player;
    }

    public void resetAllPlayerReadyStates() {
        for (Player player : playerSessionMap.values()) {
            if (Objects.equals(player.getId(), getHost().getId())) continue;
            player.setReadyFalse();
        }
    }

    public void changeQuiz(Long quizId, int questionsCount) {
        gameSetting.changeQuiz(quizId, questionsCount);
    }

    public void changeTimeLimit(TimeLimit timeLimit) {
        gameSetting.changeTimeLimit(timeLimit);
    }

    public void changeRound(int round, int questionCount) {
        gameSetting.changeRound(round, questionCount);
    }

    public Long getQuizId() {
        return gameSetting.getQuizId();
    }

    public int getTimeLimit() {
        return gameSetting.getTimeLimit();
    }

    public int getRound() {
        return gameSetting.getRound();
    }
}
