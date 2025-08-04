package io.f1.backend.global.exception.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    BAD_REQUEST_DATA("E400001", HttpStatus.BAD_REQUEST, "잘못된 요청 데이터입니다."),
    INVALID_PAGINATION("E400006", HttpStatus.BAD_REQUEST, "page와 size는 1 이상의 정수여야 합니다."),
    INTERNAL_SERVER_ERROR(
            "E500001", HttpStatus.INTERNAL_SERVER_ERROR, "서버에러가 발생했습니다. 관리자에게 문의해주세요."),
    INVALID_JSON_FORMAT("E400008", HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다. JSON 문법을 확인해주세요."),
    LOCK_ACQUISITION_FAILED("E409003", HttpStatus.CONFLICT, "다른 요청이 작업 중입니다. 잠시 후 다시 시도해주세요."),
    LOCK_INTERRUPTED("E500004", HttpStatus.INTERNAL_SERVER_ERROR, "작업 중 락 획득이 중단되었습니다. 다시 시도해주세요.");

    private final String code;

    private final HttpStatus httpStatus;

    private final String message;
}
