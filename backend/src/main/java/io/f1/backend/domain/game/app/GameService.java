package io.f1.backend.domain.game.app;

import static io.f1.backend.domain.game.mapper.RoomMapper.toGameResultListResponse;
import static io.f1.backend.domain.game.mapper.RoomMapper.toGameSettingResponse;
import static io.f1.backend.domain.game.mapper.RoomMapper.toPlayerListResponse;
import static io.f1.backend.domain.game.mapper.RoomMapper.toQuestionStartResponse;
import static io.f1.backend.domain.game.mapper.RoomMapper.toRoomSettingResponse;
import static io.f1.backend.domain.game.websocket.WebSocketUtils.getDestination;
import static io.f1.backend.domain.quiz.mapper.QuizMapper.toGameStartResponse;

import io.f1.backend.domain.game.dto.MessageType;
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
import io.f1.backend.domain.user.dto.UserPrincipal;
import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.exception.errorcode.GameErrorCode;
import io.f1.backend.global.exception.errorcode.RoomErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GameService {

    private static final int START_DELAY = 5;

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

        messageSender.send(destination, MessageType.GAME_START, toGameStartResponse(questions));
        messageSender.send(
                destination,
                MessageType.QUESTION_START,
                toQuestionStartResponse(room, START_DELAY));
    }

    public void gameEnd(Room room) {
        room.updateRoomState(RoomState.FINISHED);

        Long roomId = room.getId();
        String destination = getDestination(roomId);

        Map<String, Player> playerSessionMap = room.getPlayerSessionMap();

        messageSender.send(destination, MessageType.GAME_RESULT, toGameResultListResponse(playerSessionMap, room.getGameSetting().getRound()));

        List<Player> disconnectedPlayers = new ArrayList<>();

        room.initializeRound();
        for (Player player : playerSessionMap.values()) {
            if(player.getState().equals(ConnectionState.DISCONNECTED)) {
                disconnectedPlayers.add(player);
            }
            player.initializeCorrectCount();
            player.toggleReady();
        }

        for(Player player : disconnectedPlayers) {
            String sessionId = room.getUserIdSessionMap().get(player.id);
            roomService.exitRoomForDisconnectedPlayer(roomId, player, sessionId);
        }

        room.updateRoomState(RoomState.WAITING);
        messageSender.send(destination, MessageType.PLAYER_LIST, toPlayerListResponse(room));
        messageSender.send(destination, MessageType.GAME_SETTING, toGameSettingResponse(room.getGameSetting(), room.getCurrentQuestion()
            .getQuiz()));
        messageSender.send(destination, MessageType.ROOM_SETTING, toRoomSettingResponse(room));
    }

    private boolean validateReadyStatus(Room room) {

        Map<String, Player> playerSessionMap = room.getPlayerSessionMap();

        return playerSessionMap.values().stream().allMatch(Player::isReady);
    }

    private void validateRoomStart(Room room, UserPrincipal principal) {
        if (!Objects.equals(principal.getUserId(), room.getHost().getId())) {
            throw new CustomException(RoomErrorCode.NOT_ROOM_OWNER);
        }

        if (!validateReadyStatus(room)) {
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
}
