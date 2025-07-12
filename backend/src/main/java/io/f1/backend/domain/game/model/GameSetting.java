package io.f1.backend.domain.game.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GameSetting {

    private Long quizId;
    private Integer round; // 게임 변경 시 해당 게임의 총 문제 수로 설정
    private int timeLimit = 60;
}
