package io.f1.backend.global.exception.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum QuestionErrorCode implements ErrorCode {
    INVALID_CONTENT_LENGTH("E400011", HttpStatus.BAD_REQUEST, "문제는 5자 이상 30자 이하로 입력해주세요."),
    INVALID_ANSWER_LENGTH("E400012", HttpStatus.BAD_REQUEST, "정답은 1자 이상 30자 이하로 입력해주세요."),
    INVALID_IMAGE_QUESTION_SIZE("E400017", HttpStatus.BAD_REQUEST, "문제 수와 이미지 수가 일치하지 않습니다."),
    INVALID_IMAGE_QUESTION_FILE("E400018", HttpStatus.BAD_REQUEST, "문제에 이미지 파일이 없습니다."),
    QUESTION_NOT_FOUND("E404003", HttpStatus.NOT_FOUND, "존재하지 않는 문제입니다.");

    private final String code;

    private final HttpStatus httpStatus;

    private final String message;
}
