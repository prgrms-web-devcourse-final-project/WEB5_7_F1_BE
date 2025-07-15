package io.f1.backend.global.exception.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum QuizErrorCode implements ErrorCode {
    FILE_SIZE_TOO_LARGE("E400005", HttpStatus.BAD_REQUEST, "파일 크기가 너무 큽니다."),
    UNSUPPORTED_MEDIA_TYPE("E415001", HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 파일 형식입니다."),
    INVALID_FILTER("E400007", HttpStatus.BAD_REQUEST, "title 또는 creator 중 하나만 입력 가능합니다."),
    QUIZ_NOT_FOUND("E404002", HttpStatus.NOT_FOUND, "존재하지 않는 퀴즈입니다.");

    private final String code;

    private final HttpStatus httpStatus;

    private final String message;
}
