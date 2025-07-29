package io.f1.backend.domain.game.model;

import io.f1.backend.domain.game.dto.request.TimeLimit;
import io.f1.backend.domain.question.entity.Question;
import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.exception.errorcode.RoomErrorCode;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    private Map<Long, Player> playerMap = new ConcurrentHashMap<>();

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

    public int getCurrentUserCnt() {
        return playerMap.size();
    }

    public void addPlayer(Player player) {
        Long userId = player.getId();

        if (isHost(userId)) {
            player.toggleReady();
        }
        playerMap.put(player.getId(), player);
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

    public void removePlayer(Player removePlayer) {
        playerMap.remove(removePlayer.getId());
    }

    public void increasePlayerCorrectCount(Long userId) {
        this.playerMap.get(userId).increaseCorrectCount();
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

        for (Player player : this.playerMap.values()) {
            if (player.getState().equals(ConnectionState.DISCONNECTED)) {
                disconnectedPlayers.add(player);
            }
        }
        return disconnectedPlayers;
    }

    public void initializePlayers() {
        this.playerMap
                .values()
                .forEach(
                        player -> {
                            player.initializeCorrectCount();
                        });
        resetAllPlayerReadyStates();
    }

    public void updatePlayerConnectionState(Long userId, ConnectionState newState) {
        playerMap.get(userId).updateState(newState);
    }

    public boolean hasPlayer(Long userId) {
        return playerMap.get(userId) != null;
    }

    public boolean isLastPlayer(Player player) {
        long connectedCount = playerMap.size();
        return connectedCount == 1 && playerMap.containsKey(player.getId());
    }

    public boolean validateReadyStatus() {

        return playerMap.values().stream().allMatch(Player::isReady);
    }

    public Player getPlayerByUserId(Long userId) {
        Player player = playerMap.get(userId);
        if (player == null) {
            throw new CustomException(RoomErrorCode.PLAYER_NOT_FOUND);
        }
        return player;
    }

    public void resetAllPlayerReadyStates() {
        for (Player player : playerMap.values()) {
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

    public ConnectionState getPlayerState(Long userId) {
        return playerMap.get(userId).getState();
    }

    public boolean isSameRoom(Long otherRoomId) {
        return Objects.equals(id, otherRoomId);
    }

    public boolean isPlayerInState(Long userId, ConnectionState state) {
        return getPlayerState(userId).equals(state);
    }
}
