package io.f1.backend.domain.game.app;

import static io.f1.backend.domain.quiz.mapper.QuizMapper.toGameStartResponse;

import io.f1.backend.domain.game.dto.GameStartData;
import io.f1.backend.domain.game.dto.request.GameStartRequest;
import io.f1.backend.domain.game.dto.response.GameStartResponse;
import io.f1.backend.domain.game.event.RoomUpdatedEvent;
import io.f1.backend.domain.game.model.GameSetting;
import io.f1.backend.domain.game.model.Player;
import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.game.model.RoomState;
import io.f1.backend.domain.game.store.RoomRepository;
import io.f1.backend.domain.question.entity.Question;
import io.f1.backend.domain.quiz.app.QuizService;
import io.f1.backend.domain.quiz.entity.Quiz;
import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.exception.errorcode.GameErrorCode;
import io.f1.backend.global.exception.errorcode.RoomErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GameService {

    private final QuizService quizService;
    private final RoomRepository roomRepository;
    private final ApplicationEventPublisher eventPublisher;

    public GameStartData gameStart(Long roomId, GameStartRequest gameStartRequest) {

        Long quizId = gameStartRequest.quizId();

        Room room =
                roomRepository
                        .findRoom(roomId)
                        .orElseThrow(() -> new CustomException(RoomErrorCode.ROOM_NOT_FOUND));

        if (!validateReadyStatus(room)) {
            throw new CustomException(RoomErrorCode.PLAYER_NOT_READY);
        }

        // 방의 gameSetting에 설정된 퀴즈랑 요청 퀴즈랑 같은지 체크 후 GameSetting에서 라운드 가져오기
        Integer round = checkGameSetting(room, quizId);

        Quiz quiz = quizService.getQuizWithQuestionsById(quizId);

        // 라운드 수만큼 랜덤 Question 추출
        List<Question> questions = quizService.getRandomQuestionsWithoutAnswer(quizId, round);
        room.updateQuestions(questions);

        GameStartResponse gameStartResponse = toGameStartResponse(questions);

        // 방 정보 게임 중으로 변경
        room.updateRoomState(RoomState.PLAYING);

        eventPublisher.publishEvent(new RoomUpdatedEvent(room, quiz));

        return new GameStartData(getDestination(roomId), gameStartResponse);
    }

    private Integer checkGameSetting(Room room, Long quizId) {

        GameSetting gameSetting = room.getGameSetting();

        if (!gameSetting.validateQuizId(quizId)) {
            throw new CustomException(GameErrorCode.GAME_SETTING_CONFLICT);
        }

        return gameSetting.getRound();
    }

    private boolean validateReadyStatus(Room room) {

        Map<String, Player> playerSessionMap = room.getPlayerSessionMap();

        return playerSessionMap.values().stream().allMatch(Player::isReady);
    }

    private static String getDestination(Long roomId) {
        return "/sub/room/" + roomId;
    }
}
