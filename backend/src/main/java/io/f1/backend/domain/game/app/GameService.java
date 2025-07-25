package io.f1.backend.domain.game.app;

import static io.f1.backend.domain.game.mapper.RoomMapper.toGameSettingResponse;
import static io.f1.backend.domain.game.mapper.RoomMapper.toPlayerListResponse;
import static io.f1.backend.domain.game.mapper.RoomMapper.toQuestionStartResponse;
import static io.f1.backend.domain.quiz.mapper.QuizMapper.toGameStartResponse;

import io.f1.backend.domain.game.dto.MessageType;
import io.f1.backend.domain.game.dto.request.GameSettingChanger;
import io.f1.backend.domain.game.dto.response.PlayerListResponse;
import io.f1.backend.domain.game.event.RoomUpdatedEvent;
import io.f1.backend.domain.game.model.Player;
import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.game.model.RoomState;
import io.f1.backend.domain.game.store.RoomRepository;
import io.f1.backend.domain.game.websocket.MessageSender;
import io.f1.backend.domain.question.entity.Question;
import io.f1.backend.domain.quiz.app.QuizService;
import io.f1.backend.domain.quiz.entity.Quiz;
import io.f1.backend.domain.user.dto.UserPrincipal;
import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.exception.errorcode.GameErrorCode;
import io.f1.backend.global.exception.errorcode.RoomErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    public static final int START_DELAY = 5;

    private final MessageSender messageSender;
    private final TimerService timerService;
    private final QuizService quizService;
    private final RoomRepository roomRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void gameStart(Long roomId, UserPrincipal principal) {

        String destination = getDestination(roomId);

        Room room =
                roomRepository
                        .findRoom(roomId)
                        .orElseThrow(() -> new CustomException(RoomErrorCode.ROOM_NOT_FOUND));

        validateRoomStart(room, principal);

        Long quizId = room.getGameSetting().getQuizId();
        Quiz quiz = quizService.getQuizWithQuestionsById(quizId);
        List<Question> questions = prepareQuestions(room, quiz);

        room.updateQuestions(questions);
        room.increaseCurrentRound();
        room.updateRoomState(RoomState.PLAYING);

        eventPublisher.publishEvent(new RoomUpdatedEvent(room, quiz));

        timerService.startTimer(room, START_DELAY);

        messageSender.sendBroadcast(
                destination, MessageType.GAME_START, toGameStartResponse(questions));
        messageSender.sendBroadcast(
                destination,
                MessageType.QUESTION_START,
                toQuestionStartResponse(room, START_DELAY));
    }

    public void handlePlayerReady(Long roomId, String sessionId) {

        Room room = findRoom(roomId);

        Player player = room.getPlayerBySessionId(sessionId);

        toggleReadyIfPossible(room, player);

        String destination = getDestination(roomId);

        PlayerListResponse playerListResponse = toPlayerListResponse(room);
        log.info(playerListResponse.toString());
        messageSender.send(destination, MessageType.PLAYER_LIST, playerListResponse);
    }

    public void changeGameSetting(
            Long roomId, UserPrincipal principal, GameSettingChanger request) {
        Room room = findRoom(roomId);
        validateHostAndState(room, principal);

        if (!request.change(room, quizService)) {
            return;
        }
        request.afterChange(room, messageSender);

        broadcastGameSetting(room);

        RoomUpdatedEvent roomUpdatedEvent =
                new RoomUpdatedEvent(
                        room,
                        quizService.getQuizWithQuestionsById(room.getGameSetting().getQuizId()));

        eventPublisher.publishEvent(roomUpdatedEvent);
    }

    private void validateRoomStart(Room room, UserPrincipal principal) {
        if (!Objects.equals(principal.getUserId(), room.getHost().getId())) {
            throw new CustomException(RoomErrorCode.NOT_ROOM_OWNER);
        }

        if (!room.validateReadyStatus()) {
            throw new CustomException(GameErrorCode.PLAYER_NOT_READY);
        }

        if (room.getState() == RoomState.PLAYING) {
            throw new CustomException(RoomErrorCode.GAME_ALREADY_PLAYING);
        }
    }

    // 라운드 수만큼 랜덤 Question 추출
    private List<Question> prepareQuestions(Room room, Quiz quiz) {
        Long quizId = quiz.getId();
        Integer round = room.getGameSetting().getRound();
        return quizService.getRandomQuestionsWithoutAnswer(quizId, round);
    }

    private Room findRoom(Long roomId) {
        return roomRepository
                .findRoom(roomId)
                .orElseThrow(() -> new CustomException(RoomErrorCode.ROOM_NOT_FOUND));
    }

    private String getDestination(Long roomId) {
        return "/sub/room/" + roomId;
    }

    private void validateHostAndState(Room room, UserPrincipal principal) {
        if (!room.isHost(principal.getUserId())) {
            throw new CustomException(RoomErrorCode.NOT_ROOM_OWNER);
        }
        if (room.isPlaying()) {
            throw new CustomException(RoomErrorCode.GAME_ALREADY_PLAYING);
        }
    }

    private void toggleReadyIfPossible(Room room, Player player) {
        if (room.isPlaying()) {
            throw new CustomException(RoomErrorCode.GAME_ALREADY_PLAYING);
        }
        if (!room.isHost(player.getId())) {
            player.toggleReady();
        }
    }

    private void broadcastGameSetting(Room room) {
        String destination = getDestination(room.getId());
        Quiz quiz = quizService.getQuizWithQuestionsById(room.getGameSetting().getQuizId());
        messageSender.send(
                destination,
                MessageType.GAME_SETTING,
                toGameSettingResponse(room.getGameSetting(), quiz));
    }
}
