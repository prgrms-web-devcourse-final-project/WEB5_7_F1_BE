package io.f1.backend.domain.game.mapper;

import io.f1.backend.domain.game.dto.ChatMessage;
import io.f1.backend.domain.game.dto.Rank;
import io.f1.backend.domain.game.dto.RoomEventType;
import io.f1.backend.domain.game.dto.request.AnswerMessage;
import io.f1.backend.domain.game.dto.request.RoomCreateRequest;
import io.f1.backend.domain.game.dto.response.GameSettingResponse;
import io.f1.backend.domain.game.dto.response.PlayerListResponse;
import io.f1.backend.domain.game.dto.response.PlayerResponse;
import io.f1.backend.domain.game.dto.response.QuestionResultResponse;
import io.f1.backend.domain.game.dto.response.QuizResponse;
import io.f1.backend.domain.game.dto.response.RankUpdateResponse;
import io.f1.backend.domain.game.dto.response.RoomResponse;
import io.f1.backend.domain.game.dto.response.RoomSettingResponse;
import io.f1.backend.domain.game.dto.response.SystemNoticeResponse;
import io.f1.backend.domain.game.model.GameSetting;
import io.f1.backend.domain.game.model.Player;
import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.game.model.RoomSetting;
import io.f1.backend.domain.quiz.entity.Quiz;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

public class RoomMapper {

    private static final int DEFAULT_TIME_LIMIT = 60;

    public static RoomSetting toRoomSetting(RoomCreateRequest request) {
        return new RoomSetting(
                request.roomName(), request.maxUserCount(), request.locked(), request.password());
    }

    public static GameSetting toGameSetting(Quiz quiz) {
        return new GameSetting(quiz.getId(), quiz.getQuestions().size(), DEFAULT_TIME_LIMIT);
    }

    public static RoomSettingResponse toRoomSettingResponse(Room room) {
        return new RoomSettingResponse(
                room.getRoomSetting().roomName(),
                room.getRoomSetting().maxUserCount(),
                room.getPlayerSessionMap().size());
    }

    public static GameSettingResponse toGameSettingResponse(GameSetting gameSetting, Quiz quiz) {
        return new GameSettingResponse(
                gameSetting.getRound(), gameSetting.getTimeLimit(), toQuizResponse(quiz));
    }

    public static PlayerListResponse toPlayerListResponse(Room room) {
        List<PlayerResponse> playerResponseList =
                room.getPlayerSessionMap().values().stream()
                        .map(player -> new PlayerResponse(player.getNickname(), false))
                        .toList();

        return new PlayerListResponse(room.getHost().getNickname(), playerResponseList);
    }

    public static RoomResponse toRoomResponse(Room room, Quiz quiz) {
        return new RoomResponse(
                room.getId(),
                room.getRoomSetting().roomName(),
                room.getRoomSetting().maxUserCount(),
                room.getPlayerSessionMap().size(),
                room.getRoomSetting().locked(),
                room.getState().name(),
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.getCreator().getNickname(),
                quiz.getQuestions().size(),
                quiz.getThumbnailUrl());
    }

    public static QuizResponse toQuizResponse(Quiz quiz) {
        return new QuizResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.getThumbnailUrl(),
                quiz.getQuestions().size());
    }

    public static SystemNoticeResponse ofPlayerEvent(String nickname, RoomEventType roomEventType) {
        String message = "";
        if (roomEventType == RoomEventType.ENTER) {
            message = " 님이 입장하셨습니다";
        } else if (roomEventType == RoomEventType.EXIT) {
            message = " 님이 퇴장하셨습니다";
        }
        return new SystemNoticeResponse(nickname + message, Instant.now());
    }

    public static ChatMessage toChatMessage(AnswerMessage answerMessage) {
        return new ChatMessage(
                answerMessage.nickname(), answerMessage.message(), answerMessage.timestamp());
    }

    public static QuestionResultResponse toQuestionResultResponse(
            AnswerMessage chatMessage, String answer) {
        return new QuestionResultResponse(chatMessage.questionId(), chatMessage.nickname(), answer);
    }

    public static RankUpdateResponse toRankUpdateResponse(Room room) {
        return new RankUpdateResponse(
                room.getPlayerSessionMap().values().stream()
                        .sorted(Comparator.comparing(Player::getCorrectCount).reversed())
                        .map(player -> new Rank(player.getNickname(), player.getCorrectCount()))
                        .toList());
    }
}
