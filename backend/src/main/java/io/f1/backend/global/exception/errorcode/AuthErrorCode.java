package io.f1.backend.global.exception.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
    UNAUTHORIZED("E401001", HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    AUTH_SESSION_NOT_FOUND("E401002", HttpStatus.UNAUTHORIZED, "세션이 존재하지 않습니다. 로그인 후 이용해주세요."),
    AUTH_SESSION_EXPIRED("E401003", HttpStatus.UNAUTHORIZED, "세션이 만료되었습니다. 다시 로그인해주세요."),
    AUTH_SESSION_LOST("E401004", HttpStatus.UNAUTHORIZED, "세션 정보가 유실되었습니다. 다시 로그인해주세요."),
    FORBIDDEN("E403001", HttpStatus.FORBIDDEN, "권한이 없습니다."),

    LOGIN_FAILED("E401005", HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다.");

    private final String code;

    private final HttpStatus httpStatus;

    private final String message;
}
