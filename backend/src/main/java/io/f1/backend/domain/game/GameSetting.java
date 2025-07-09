package io.f1.backend.domain.game;

public class GameSetting {

	private Long quizId;
	private Integer round; // 게임 변경 시 해당 게임의 총 문제 수로 설정
	private int timeLimit = 60;
}
