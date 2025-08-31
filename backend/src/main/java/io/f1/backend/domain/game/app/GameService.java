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
import io.f1.backend.domain.game.dto.response.GameResultListResponse;
import io.f1.backend.domain.game.dto.response.GameResultResponse;
import io.f1.backend.domain.game.event.GameCorrectAnswerEvent;
import io.f1.backend.domain.game.event.GameTimeoutEvent;
import io.f1.backend.domain.game.event.RoomUpdatedEvent;
import io.f1.backend.domain.game.model.ConnectionState;
import io.f1.backend.domain.game.model.Player;
import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.game.model.RoomState;
import io.f1.backend.domain.game.store.RoomRepository;
import io.f1.backend.domain.game.websocket.MessageSender;
import io.f1.backend.domain.question.entity.Question;
import io.f1.backend.domain.quiz.app.QuizService;
import io.f1.backend.domain.quiz.entity.Quiz;
import io.f1.backend.domain.stat.app.StatService;
import io.f1.backend.domain.user.dto.UserPrincipal;
import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.exception.errorcode.GameErrorCode;
import io.f1.backend.global.exception.errorcode.RoomErrorCode;
import io.f1.backend.global.lock.DistributedLock;

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

    private final StatService statService;
    private final QuizService quizService;
    private final RoomService roomService;
    private final TimerService timerService;
    private final MessageSender messageSender;
    private final RoomRepository roomRepository;
    private final ApplicationEventPublisher eventPublisher;

    @DistributedLock(prefix = "room", key = "#roomId")
    public void gameStart(Long roomId, UserPrincipal principal) {

        String destination = getDestination(roomId);

        Room room =
                roomRepository
                        .findRoom(roomId)
                        .orElseThrow(() -> new CustomException(RoomErrorCode.ROOM_NOT_FOUND));

        validateRoomStart(room, principal);

        Long quizId = room.getGameSetting().getQuizId();
        Quiz quiz = quizService.findQuizById(quizId);
        Long questionsCount = quizService.getQuestionsCount(quizId);
        List<Question> questions = prepareQuestions(room, quiz);

        room.updateQuestions(questions);
        room.increaseCurrentRound();
        room.updateRoomState(RoomState.PLAYING);

        eventPublisher.publishEvent(new RoomUpdatedEvent(room, quiz, questionsCount));

        timerService.startTimer(room, START_DELAY);

        messageSender.sendBroadcast(
                destination,
                MessageType.GAME_START,
                toGameStartResponse(quiz.getQuizType(), questions));
        messageSender.sendBroadcast(
                destination, MessageType.RANK_UPDATE, toRankUpdateResponse(room));
        messageSender.sendBroadcast(
                destination,
                MessageType.QUESTION_START,
                toQuestionStartResponse(room, START_DELAY));
    }

    @EventListener
    public void onCorrectAnswer(GameCorrectAnswerEvent event) {

        Room room = event.room();
        log.debug(room.getId() + "번 방 채팅으로 정답! 현재 라운드 : " + room.getCurrentRound());
        Long userId = event.userId();
        ChatMessage chatMessage = event.chatMessage();
        String answer = event.answer();

        String destination = getDestination(room.getId());

        room.increasePlayerCorrectCount(userId);

        messageSender.sendBroadcast(
                destination,
                MessageType.QUESTION_RESULT,
                toQuestionResultResponse(chatMessage.nickname(), answer));
        messageSender.sendBroadcast(
                destination, MessageType.RANK_UPDATE, toRankUpdateResponse(room));
        messageSender.sendBroadcast(
                destination,
                MessageType.SYSTEM_NOTICE,
                ofPlayerEvent(chatMessage.nickname(), RoomEventType.CORRECT_ANSWER));

        timerService.cancelTimer(room);
        room.compareAndSetAnsweredFlag(true, false);

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

        // false -> true 여야 하는데 실패했을 때 => 이미 정답 처리가 된 경우 (onCorrectAnswer 로직 실행 중)
        if (!room.compareAndSetAnsweredFlag(false, true)) {
            return;
        }

        log.debug(room.getId() + "번 방 타임아웃! 현재 라운드 : " + room.getCurrentRound());

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

        room.compareAndSetAnsweredFlag(true, false);
    }

    public void gameEnd(Room room) {
        Long roomId = room.getId();
        String destination = getDestination(roomId);

        Map<Long, Player> playerMap = room.getPlayerMap();

        GameResultListResponse gameResultListResponse =
                toGameResultListResponse(playerMap, room.getGameSetting().getRound());

        messageSender.sendBroadcast(destination, MessageType.GAME_RESULT, gameResultListResponse);

        updateRank(room, gameResultListResponse);

        room.initializeRound();
        room.initializePlayers();

        List<Player> disconnectedPlayers = room.getDisconnectedPlayers();

        if (!disconnectedPlayers.isEmpty()) {
            roomService.handleDisconnectedPlayers(room, disconnectedPlayers);
        } else {
            messageSender.sendBroadcast(
                    destination, MessageType.PLAYER_LIST, toPlayerListResponse(room));
        }

        room.updateRoomState(RoomState.WAITING);

        messageSender.sendBroadcast(
                destination,
                MessageType.GAME_SETTING,
                toGameSettingResponse(
                        room.getGameSetting(),
                        quizService.findQuizById(room.getGameSetting().getQuizId()),
                        room.getRound()));
        messageSender.sendBroadcast(
                destination, MessageType.ROOM_SETTING, toRoomSettingResponse(room));
    }

    private void updateRank(Room room, GameResultListResponse gameResultListResponse) {

        List<GameResultResponse> result = gameResultListResponse.result();

        for (GameResultResponse gameResultResponse : result) {
            Long playerId = gameResultResponse.id();
            int rank = gameResultResponse.rank();
            int score = gameResultResponse.score();

            Player player = room.getPlayerByUserId(playerId);

            if (room.isPlayerInState(playerId, ConnectionState.DISCONNECTED)) {
                statService.updateRank(playerId, false, 0);
                continue;
            }

            if (rank == 1) {
                statService.updateRank(playerId, true, score);
                continue;
            }

            statService.updateRank(playerId, false, score);
        }
    }

    @DistributedLock(prefix = "room", key = "#roomId")
    public void handlePlayerReady(Long roomId, UserPrincipal userPrincipal) {

        Room room = findRoom(roomId);

        Player player = room.getPlayerByUserId(userPrincipal.getUserId());

        toggleReadyIfPossible(room, player);

        String destination = getDestination(roomId);

        messageSender.sendBroadcast(
                destination, MessageType.PLAYER_LIST, toPlayerListResponse(room));
    }

    public void changeGameSetting(
            Long roomId, UserPrincipal principal, GameSettingChanger request) {
        Room room = findRoom(roomId);
        validateHostAndState(room, principal);

        if (!request.change(room, quizService)) {
            return;
        }
        request.afterChange(room, messageSender, eventPublisher, quizService);

        broadcastGameSetting(room);
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
        Integer round = room.getRound();
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
        Quiz quiz = quizService.findQuizById(room.getGameSetting().getQuizId());
        Long questionsCount = quizService.getQuestionsCount(quiz.getId());
        messageSender.sendBroadcast(
                destination,
                MessageType.GAME_SETTING,
                toGameSettingResponse(room.getGameSetting(), quiz, questionsCount));
    }
}
