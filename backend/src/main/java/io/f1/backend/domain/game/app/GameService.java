package io.f1.backend.domain.game.app;

import static io.f1.backend.domain.quiz.mapper.QuizMapper.toGameStartResponse;

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
import io.f1.backend.domain.user.dto.UserPrincipal;
import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.exception.errorcode.RoomErrorCode;

import java.util.Objects;
import lombok.RequiredArgsConstructor;

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

    public GameStartResponse gameStart(Long roomId, UserPrincipal principal) {

        Room room =
                roomRepository
                        .findRoom(roomId)
                        .orElseThrow(() -> new CustomException(RoomErrorCode.ROOM_NOT_FOUND));

        validateRoomStart(room, principal);

        Long quizId = room.getGameSetting().getQuizId();
        Quiz quiz = quizService.getQuizWithQuestionsById(quizId);
        List<Question> questions = prepareQuestions(room, quiz);

        room.updateQuestions(questions);

        // 방 정보 게임 중으로 변경
        room.updateRoomState(RoomState.PLAYING);

        eventPublisher.publishEvent(new RoomUpdatedEvent(room, quiz));

        return toGameStartResponse(questions);
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
            throw new CustomException(RoomErrorCode.PLAYER_NOT_READY);
        }

        if (room.getState() == RoomState.PLAYING) {
            throw new CustomException(RoomErrorCode.GAME_ALREADY_PLAYING);
        }
    }

    // 라운드 수만큼 랜덤 Question 추출
    public List<Question> prepareQuestions(Room room, Quiz quiz) {
        Long quizId = quiz.getId();
        Integer round = room.getGameSetting().getRound();
        return quizService.getRandomQuestionsWithoutAnswer(quizId, round);
    }
}
