package io.f1.backend.domain.game.model;

import io.f1.backend.domain.question.entity.Question;
import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.exception.errorcode.RoomErrorCode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;

@Getter
public class Room {

    private static final String PENDING_SESSION_ID = "PENDING_SESSION_ID";

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

    public Room(Long id, RoomSetting roomSetting, GameSetting gameSetting, Player host) {
        this.id = id;
        this.roomSetting = roomSetting;
        this.gameSetting = gameSetting;
        this.host = host;
    }

    public boolean  addValidatedUserIds (Long userId) {
        return validatedUserIds.add(userId);
    }

    public int getCurrentUserCnt(){
        return validatedUserIds.size();
    }

    public void addPlayer(Long userId,String sessionId, Player player) {

        if(!validatedUserIds.contains(userId)){
            throw new CustomException(RoomErrorCode.ROOM_ENTER_REQUIRED);
        }

        if (isHost(player.getId())) {
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

    public void removeSessionId(String sessionId) {
        this.playerSessionMap.remove(sessionId);
    }

    public void removeUserId(Long userId){
        validatedUserIds.remove(userId);
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

    public void increaseCorrectCount() {
        currentRound++;
    }

    public void reconnectSession(String oldSessionId ,String newSessionId) {
        Player player = playerSessionMap.get(oldSessionId);
        removeSessionId(oldSessionId);
        playerSessionMap.put(newSessionId, player);
    }

    public void updatePlayerConnectionState(String sessionId, ConnectionState newState) {
        playerSessionMap.get(sessionId).updateState(newState);
    }

    public ConnectionState getPlayerConnectionState(String sessionId) {
        return playerSessionMap.get(sessionId).getState();
    }

    public boolean isReconnectTarget(String sessionId) {
        return playerSessionMap.get(sessionId) != null;
    }

}

