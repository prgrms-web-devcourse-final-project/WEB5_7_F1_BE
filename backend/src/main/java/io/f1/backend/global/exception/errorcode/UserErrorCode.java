package io.f1.backend.global.exception.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    NICKNAME_EMPTY("E400002", HttpStatus.BAD_REQUEST, "닉네임은 필수 입력입니다."),
    NICKNAME_TOO_LONG("E400003", HttpStatus.BAD_REQUEST, "닉네임은 6글자 이하로 입력해야 합니다."),
    NICKNAME_NOT_ALLOWED("E400004", HttpStatus.BAD_REQUEST, "한글, 영문, 숫자만 입력해주세요."),
    NICKNAME_CONFLICT("E409001", HttpStatus.CONFLICT, "중복된 닉네임입니다."),
    USER_NOT_FOUND("E404001", HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다.");

    private final String code;

    private final HttpStatus httpStatus;

    private final String message;
}
