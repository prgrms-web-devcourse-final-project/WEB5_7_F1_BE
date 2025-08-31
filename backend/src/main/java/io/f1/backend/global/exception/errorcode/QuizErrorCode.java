package io.f1.backend.global.exception.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum QuizErrorCode implements ErrorCode {
    FILE_SIZE_TOO_LARGE("E400005", HttpStatus.BAD_REQUEST, "파일 크기가 너무 큽니다."),
    INVALID_TITLE_LENGTH("E400009", HttpStatus.BAD_REQUEST, "제목은 2자 이상 30자 이하로 입력해주세요."),
    INVALID_DESC_LENGTH("E400010", HttpStatus.BAD_REQUEST, "설명은 10자 이상 50자 이하로 입력해주세요."),
    UNSUPPORTED_IMAGE_FORMAT(
            "E400013", HttpStatus.BAD_REQUEST, "지원하지 않는 이미지 형식입니다. (jpg, jpeg, png, webp 만 가능)"),
    UNSUPPORTED_MEDIA_TYPE("E415001", HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 파일 형식입니다."),
    INVALID_FILTER("E400007", HttpStatus.BAD_REQUEST, "title 또는 creator 중 하나만 입력 가능합니다."),
    QUIZ_NOT_FOUND("E404002", HttpStatus.NOT_FOUND, "존재하지 않는 퀴즈입니다."),
    IMAGE_SAVE_FAILED("E500002", HttpStatus.INTERNAL_SERVER_ERROR, "이미지 저장에 실패했습니다."),
    IMAGE_DELETE_FAILED("E500003", HttpStatus.INTERNAL_SERVER_ERROR, "이미지 삭제에 실패했습니다.");

    private final String code;

    private final HttpStatus httpStatus;

    private final String message;
}
