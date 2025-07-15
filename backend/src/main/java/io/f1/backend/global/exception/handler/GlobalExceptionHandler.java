package io.f1.backend.global.exception.handler;

import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.exception.errorcode.CommonErrorCode;
import io.f1.backend.global.exception.errorcode.ErrorCode;
import io.f1.backend.global.exception.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();

        ErrorResponse response = new ErrorResponse(
            errorCode.getCode(),
            errorCode.getMessage()
        );
        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;

        ErrorResponse response = new ErrorResponse(
            errorCode.getCode(),
            errorCode.getMessage()
        );
        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        CommonErrorCode code = CommonErrorCode.BAD_REQUEST_DATA;

        String message = e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .findFirst()
            .orElse(code.getMessage());

        ErrorResponse response = new ErrorResponse(
            code.getCode(),
            message
        );

        return new ResponseEntity<>(response, code.getHttpStatus());

    }
}
