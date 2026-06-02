package com.damu.PaymentService.exception;

import lombok.Builder;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Log4j2
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception) {
        log.warn("Payment service validation exception message={}", exception.getMessage());
        return new ResponseEntity<>(ErrorResponse.builder()
                .errorCode("INVALID_REQUEST")
                .errorMessage(exception.getMessage())
                .build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        log.error("Payment service unexpected exception message={}", exception.getMessage(), exception);
        return new ResponseEntity<>(ErrorResponse.builder()
                .errorCode("INTERNAL_SERVER_ERROR")
                .errorMessage("Internal Server Error")
                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Data
    @Builder
    public static class ErrorResponse {
        private String errorCode;
        private String errorMessage;
    }
}
