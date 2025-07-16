package io.f1.backend.domain.game.app;

import io.f1.backend.domain.game.dto.GameStartData;
import io.f1.backend.domain.game.dto.response.GameStartResponse;
import io.f1.backend.domain.game.event.RoomUpdatedEvent;
import io.f1.backend.domain.game.model.GameSetting;
import io.f1.backend.domain.game.model.Player;
import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.game.model.RoomState;
import io.f1.backend.domain.game.store.RoomRepository;
import io.f1.backend.domain.quiz.app.QuizService;
import io.f1.backend.domain.quiz.entity.Quiz;

import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GameService {

    private final QuizService quizService;
    private final RoomRepository roomRepository;
    private final ApplicationEventPublisher eventPublisher;

    public GameStartData gameStart(Long roomId, Long quizId) {

        Room room =
                roomRepository
                        .findRoom(roomId)
                        .orElseThrow(() -> new IllegalArgumentException("404 존재하지 않는 방입니다."));

        if (!validateReadyStatus(room)) {
            throw new IllegalArgumentException("E403004 : 레디 상태가 아닙니다.");
        }

        // 방의 gameSetting에 설정된 퀴즈랑 요청 퀴즈랑 같은지 체크 후 GameSetting에서 라운드 가져오기
        Integer round = checkGameSetting(room, quizId);

        Quiz quiz = quizService.getQuizWithQuestionsById(quizId);

        // 라운드 수만큼 랜덤 Question 추출
        GameStartResponse questions = quizService.getRandomQuestionsWithoutAnswer(quizId, round);

        // 방 정보 게임 중으로 변경
        room.updateRoomState(RoomState.PLAYING);

        eventPublisher.publishEvent(new RoomUpdatedEvent(room, quiz));

        return new GameStartData(getDestination(roomId), questions);
    }

    private Integer checkGameSetting(Room room, Long quizId) {

        GameSetting gameSetting = room.getGameSetting();

        if (!gameSetting.checkQuizId(quizId)) {
            throw new IllegalArgumentException("E409002 : 게임 설정이 다릅니다. (게임을 시작할 수 없습니다.)");
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
