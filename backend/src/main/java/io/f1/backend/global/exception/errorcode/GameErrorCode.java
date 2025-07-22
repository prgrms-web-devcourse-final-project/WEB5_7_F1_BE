package io.f1.backend.global.exception.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GameErrorCode implements ErrorCode {
    GAME_SETTING_CONFLICT("E409002", HttpStatus.CONFLICT, "게임 설정이 맞지 않습니다."),
    PLAYER_NOT_READY("E403004", HttpStatus.FORBIDDEN, "게임 시작을 위한 준비 상태가 아닙니다."),
    ROUND_EXCEEDS_QUESTION_COUNT("E400016", HttpStatus.BAD_REQUEST, "라운드 수는 선택한 퀴즈의 문제 수보다 많을 수 없습니다.");

    private final String code;

    private final HttpStatus httpStatus;

    private final String message;
}
