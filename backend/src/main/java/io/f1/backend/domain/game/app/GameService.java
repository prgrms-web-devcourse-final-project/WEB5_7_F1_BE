package io.f1.backend.domain.game.app;

import static io.f1.backend.domain.game.mapper.RoomMapper.ofPlayerEvent;
import static io.f1.backend.domain.game.mapper.RoomMapper.toGameResultListResponse;
import static io.f1.backend.domain.game.mapper.RoomMapper.toGameSettingResponse;
import static io.f1.backend.domain.game.mapper.RoomMapper.toPlayerListResponse;
import static io.f1.backend.domain.game.mapper.RoomMapper.toQuestionResultResponse;
import static io.f1.backend.domain.game.mapper.RoomMapper.toQuestionStartResponse;
import static io.f1.backend.domain.game.mapper.RoomMapper.toRankUpdateResponse;
import static io.f1.backend.domain.game.mapper.RoomMapper.toRoomSettingResponse;
import static io.f1.backend.domain.game.websocket.WebSocketUtils.getDestination;
import static io.f1.backend.domain.quiz.mapper.QuizMapper.toGameStartResponse;

import io.f1.backend.domain.game.dto.ChatMessage;
import io.f1.backend.domain.game.dto.MessageType;
import io.f1.backend.domain.game.dto.RoomEventType;
import io.f1.backend.domain.game.dto.request.GameSettingChanger;
import io.f1.backend.domain.game.dto.response.PlayerListResponse;
import io.f1.backend.domain.game.event.GameCorrectAnswerEvent;
import io.f1.backend.domain.game.event.GameTimeoutEvent;
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
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private static final int START_DELAY = 5;
    private static final int CONTINUE_DELAY = 3;
    private static final String NONE_CORRECT_USER = "";

    private final QuizService quizService;
    private final RoomService roomService;
    private final TimerService timerService;
    private final MessageSender messageSender;
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

        messageSender.sendBroadcast(destination, MessageType.GAME_START, toGameStartResponse(questions));
        messageSender.sendBroadcast(destination, MessageType.RANK_UPDATE, toRankUpdateResponse(room));
        messageSender.sendBroadcast(
            destination,
            MessageType.QUESTION_START,
            toQuestionStartResponse(room, START_DELAY));
    }

    @EventListener
    public void onCorrectAnswer(GameCorrectAnswerEvent event) {

        Room room = event.room();
        String sessionId = event.sessionId();
        ChatMessage chatMessage = event.chatMessage();
        String answer = event.answer();

        String destination = getDestination(room.getId());

        room.increasePlayerCorrectCount(sessionId);

        messageSender.sendBroadcast(
                destination,
                MessageType.QUESTION_RESULT,
                toQuestionResultResponse(chatMessage.nickname(), answer));
        messageSender.sendBroadcast(destination, MessageType.RANK_UPDATE, toRankUpdateResponse(room));
        messageSender.sendBroadcast(
                destination,
                MessageType.SYSTEM_NOTICE,
                ofPlayerEvent(chatMessage.nickname(), RoomEventType.CORRECT_ANSWER));

        timerService.cancelTimer(room);

        if (!timerService.validateCurrentRound(room)) {
            gameEnd(room);
            return;
        }

        room.increaseCurrentRound();

        // 타이머 추가하기
        timerService.startTimer(room, CONTINUE_DELAY);
        messageSender.sendBroadcast(
                destination,
                MessageType.QUESTION_START,
                toQuestionStartResponse(room, CONTINUE_DELAY));
    }

    @EventListener
    public void onTimeout(GameTimeoutEvent event) {
        Room room = event.room();
        String destination = getDestination(room.getId());

        messageSender.sendBroadcast(
                destination,
                MessageType.QUESTION_RESULT,
                toQuestionResultResponse(NONE_CORRECT_USER, room.getCurrentQuestion().getAnswer()));
        messageSender.sendBroadcast(
                destination,
                MessageType.SYSTEM_NOTICE,
                ofPlayerEvent(NONE_CORRECT_USER, RoomEventType.TIMEOUT));

        if (!timerService.validateCurrentRound(room)) {
            gameEnd(room);
            return;
        }

        room.increaseCurrentRound();

        timerService.startTimer(room, CONTINUE_DELAY);
        messageSender.sendBroadcast(
                destination,
                MessageType.QUESTION_START,
                toQuestionStartResponse(room, CONTINUE_DELAY));
    }

    public void gameEnd(Room room) {
        Long roomId = room.getId();
        String destination = getDestination(roomId);

        Map<String, Player> playerSessionMap = room.getPlayerSessionMap();

        // TODO : 랭킹 정보 업데이트
        messageSender.sendBroadcast(
                destination,
                MessageType.GAME_RESULT,
                toGameResultListResponse(playerSessionMap, room.getGameSetting().getRound()));

        room.initializeRound();
        room.initializePlayers();

        List<Player> disconnectedPlayers = room.getDisconnectedPlayers();
        roomService.handleDisconnectedPlayers(room, disconnectedPlayers);

        room.updateRoomState(RoomState.WAITING);

        messageSender.sendBroadcast(
                destination,
                MessageType.GAME_SETTING,
                toGameSettingResponse(
                        room.getGameSetting(),
                        quizService.getQuizWithQuestionsById(room.getGameSetting().getQuizId())));
        messageSender.sendBroadcast(destination, MessageType.ROOM_SETTING, toRoomSettingResponse(room));
    }

    public void handlePlayerReady(Long roomId, String sessionId) {

        Room room = findRoom(roomId);

        Player player = room.getPlayerBySessionId(sessionId);

        toggleReadyIfPossible(room, player);

        String destination = getDestination(roomId);

        PlayerListResponse playerListResponse = toPlayerListResponse(room);
        log.info(playerListResponse.toString());
        messageSender.sendBroadcast(destination, MessageType.PLAYER_LIST, playerListResponse);
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
        messageSender.sendBroadcast(
                destination,
                MessageType.GAME_SETTING,
                toGameSettingResponse(room.getGameSetting(), quiz));
    }
}
