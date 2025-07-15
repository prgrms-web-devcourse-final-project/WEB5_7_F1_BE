package io.f1.backend.global.exception.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum QuestionErrorCode implements ErrorCode {

    QUESTION_NOT_FOUND("E404003", HttpStatus.NOT_FOUND, "존재하지 않는 문제입니다.");

    private final String code;

    private final HttpStatus httpStatus;

    private final String message;

}
