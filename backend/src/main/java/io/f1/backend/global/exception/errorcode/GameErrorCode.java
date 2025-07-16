package io.f1.backend.global.exception.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GameErrorCode implements ErrorCode {

    GAME_SETTING_CONFLICT("E409002", HttpStatus.CONFLICT, "게임 설정이 맞지 않습니다.");

    private final String code;

    private final HttpStatus httpStatus;

    private final String message;
}
