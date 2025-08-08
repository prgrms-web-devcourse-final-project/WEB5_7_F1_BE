package io.f1.backend.domain.game.mapper;

import io.f1.backend.domain.game.dto.Rank;
import io.f1.backend.domain.game.dto.RoomEventType;
import io.f1.backend.domain.game.dto.request.RoomCreateRequest;
import io.f1.backend.domain.game.dto.response.GameResultListResponse;
import io.f1.backend.domain.game.dto.response.GameResultResponse;
import io.f1.backend.domain.game.dto.response.GameSettingResponse;
import io.f1.backend.domain.game.dto.response.PlayerListResponse;
import io.f1.backend.domain.game.dto.response.PlayerResponse;
import io.f1.backend.domain.game.dto.response.QuestionResultResponse;
import io.f1.backend.domain.game.dto.response.QuestionStartResponse;
import io.f1.backend.domain.game.dto.response.QuizResponse;
import io.f1.backend.domain.game.dto.response.RankUpdateResponse;
import io.f1.backend.domain.game.dto.response.RoomResponse;
import io.f1.backend.domain.game.dto.response.RoomSettingResponse;
import io.f1.backend.domain.game.dto.response.SystemNoticeResponse;
import io.f1.backend.domain.game.model.GameSetting;
import io.f1.backend.domain.game.model.Player;
import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.game.model.RoomSetting;
import io.f1.backend.domain.quiz.dto.QuizMinData;
import io.f1.backend.domain.quiz.entity.Quiz;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class RoomMapper {

    private static final int DEFAULT_TIME_LIMIT = 60;

    public static RoomSetting toRoomSetting(RoomCreateRequest request) {
        return new RoomSetting(
                request.roomName(), request.maxUserCount(), request.locked(), request.password());
    }

    public static GameSetting toGameSetting(QuizMinData quizMinData) {
        return new GameSetting(
                quizMinData.quizMinId(),
                quizMinData.questionCount().intValue(),
                DEFAULT_TIME_LIMIT);
    }

    public static RoomSettingResponse toRoomSettingResponse(Room room) {
        return new RoomSettingResponse(
                room.getRoomSetting().roomName(),
                room.getRoomSetting().maxUserCount(),
                room.getRoomSetting().locked());
    }

    public static GameSettingResponse toGameSettingResponse(
            GameSetting gameSetting, Quiz quiz, long questionsCount) {
        return new GameSettingResponse(
                gameSetting.getRound(),
                gameSetting.getTimeLimit(),
                toQuizResponse(quiz, questionsCount));
    }

    public static PlayerListResponse toPlayerListResponse(Room room) {
        List<PlayerResponse> playerResponseList =
                room.getPlayerMap().values().stream()
                        .map(player -> new PlayerResponse(player.getNickname(), player.isReady()))
                        .toList();

        return new PlayerListResponse(
                room.getHost().getNickname(), playerResponseList, playerResponseList.size());
    }

    public static RoomResponse toRoomResponse(Room room, Quiz quiz, long questionsCount) {
        return new RoomResponse(
                room.getId(),
                room.getRoomSetting().roomName(),
                room.getRoomSetting().maxUserCount(),
                room.getCurrentUserCnt(),
                room.getRoomSetting().locked(),
                room.getState().name(),
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.getCreator().getNickname(),
                (int) questionsCount,
                quiz.getThumbnailUrl());
    }

    public static QuizResponse toQuizResponse(Quiz quiz, long questionsCount) {
        return new QuizResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.getThumbnailUrl(),
                (int) questionsCount);
    }

    public static SystemNoticeResponse ofPlayerEvent(String nickname, RoomEventType roomEventType) {
        return new SystemNoticeResponse(roomEventType.getMessage(nickname), Instant.now());
    }

    public static QuestionResultResponse toQuestionResultResponse(String nickname, String answer) {
        return new QuestionResultResponse(nickname, answer);
    }

    public static RankUpdateResponse toRankUpdateResponse(Room room) {
        return new RankUpdateResponse(
                room.getPlayerMap().values().stream()
                        .sorted(Comparator.comparing(Player::getCorrectCount).reversed())
                        .map(player -> new Rank(player.getNickname(), player.getCorrectCount()))
                        .toList());
    }

    public static QuestionStartResponse toQuestionStartResponse(Room room, int delay) {
        return new QuestionStartResponse(
                room.getCurrentQuestion().getId(),
                room.getCurrentRound(),
                Instant.now().plusSeconds(delay),
                room.getTimeLimit(),
                Instant.now(),
                room.getRound());
    }

    public static GameResultResponse toGameResultResponse(
            Player player, int round, int rank, int totalPlayers) {
        double correctRate = (double) player.getCorrectCount() / round;
        int score = (int) (correctRate * 100) + (totalPlayers - rank) * 5;

        return new GameResultResponse(
                player.id, player.nickname, score, player.getCorrectCount(), rank);
    }

    public static GameResultListResponse toGameResultListResponse(
            Map<Long, Player> playerMap, int round) {

        List<Player> rankedPlayers =
                playerMap.values().stream()
                        .sorted(Comparator.comparingInt(Player::getCorrectCount).reversed())
                        .toList();

        List<GameResultResponse> gameResults = buildRankedGameResults(rankedPlayers, round);

        return new GameResultListResponse(gameResults);
    }

    private static List<GameResultResponse> buildRankedGameResults(
            List<Player> rankedPlayers, int round) {
        int totalPlayers = rankedPlayers.size();
        int prevCorrectCnt = -1;
        int rank = 0;

        List<GameResultResponse> results = new ArrayList<>();
        for (int i = 0; i < totalPlayers; i++) {
            Player player = rankedPlayers.get(i);

            int correctCnt = player.getCorrectCount();

            if (prevCorrectCnt != correctCnt) {
                rank = i + 1;
            }

            results.add(toGameResultResponse(player, round, rank, totalPlayers));
            prevCorrectCnt = correctCnt;
        }
        return results;
    }
}
