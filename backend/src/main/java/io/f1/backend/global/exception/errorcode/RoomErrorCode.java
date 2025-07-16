package io.f1.backend.global.exception.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RoomErrorCode implements ErrorCode {
    ROOM_USER_LIMIT_REACHED("E403002", HttpStatus.FORBIDDEN, "정원이 모두 찼습니다."),
    ROOM_GAME_IN_PROGRESS("E403003", HttpStatus.FORBIDDEN, "게임이 진행 중 입니다."),
    ROOM_NOT_FOUND("E404005", HttpStatus.NOT_FOUND, "존재하지 않는 방입니다."),
    WRONG_PASSWORD("E401006", HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지않습니다."),
    SOCKET_SESSION_NOT_FOUND("E404006",HttpStatus.NOT_FOUND,"존재하지 않는 소켓 세션입니다.");

    private final String code;

    private final HttpStatus httpStatus;

    private final String message;
}
