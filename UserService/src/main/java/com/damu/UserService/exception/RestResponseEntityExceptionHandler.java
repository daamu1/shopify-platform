package com.damu.UserService.exception;

import com.damu.UserService.model.ErrorResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Log4j2
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UserServiceException.class)
    public ResponseEntity<ErrorResponse> handleUserServiceException(UserServiceException exception) {
        log.warn("User service exception errorCode={} status={} message={}",
                exception.getErrorCode(), exception.getStatus(), exception.getMessage());
        return new ResponseEntity<>(ErrorResponse.builder()
                .errorCode(exception.getErrorCode())
                .errorMessage(exception.getMessage())
                .build(), HttpStatus.valueOf(exception.getStatus()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        log.error("User service unexpected exception message={}", exception.getMessage(), exception);
        return new ResponseEntity<>(ErrorResponse.builder()
                .errorCode("INTERNAL_SERVER_ERROR")
                .errorMessage("Internal Server Error")
                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
