package io.f1.backend.global.exception.handler;

import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.exception.errorcode.CommonErrorCode;
import io.f1.backend.global.exception.errorcode.ErrorCode;
import io.f1.backend.global.exception.response.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        log.warn(e.getMessage());
        ErrorCode errorCode = e.getErrorCode();

        ErrorResponse response = new ErrorResponse(errorCode.getCode(), errorCode.getMessage());
        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
        log.warn("handleException: {}", e.getMessage());

        if ("text/event-stream".equals(request.getHeader("Accept"))) {
            return ResponseEntity.noContent().build();
        }

        CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;

        ErrorResponse response = new ErrorResponse(errorCode.getCode(), errorCode.getMessage());
        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        log.warn("MethodArgumentNotValidException: {}", e.getMessage());
        CommonErrorCode code = CommonErrorCode.BAD_REQUEST_DATA;

        String message =
                e.getBindingResult().getFieldErrors().stream()
                        .map(FieldError::getDefaultMessage)
                        .findFirst()
                        .orElse(code.getMessage());

        ErrorResponse response = new ErrorResponse(code.getCode(), message);

        return new ResponseEntity<>(response, code.getHttpStatus());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e) {
        log.warn("HttpMessageNotReadableException: {}", e.getMessage());
        CommonErrorCode code = CommonErrorCode.INVALID_JSON_FORMAT;

        ErrorResponse response = new ErrorResponse(code.getCode(), code.getMessage());

        return new ResponseEntity<>(response, code.getHttpStatus());
    }
}
