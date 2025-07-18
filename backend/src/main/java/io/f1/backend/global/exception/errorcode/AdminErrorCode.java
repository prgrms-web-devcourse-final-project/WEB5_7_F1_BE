package io.f1.backend.global.exception.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AdminErrorCode implements ErrorCode {
    ADMIN_NOT_FOUND("E404007", HttpStatus.NOT_FOUND, "존재하지 않는 관리자 입니다");

    private final String code;

    private final HttpStatus httpStatus;

    private final String message;
}
