package com.damu.OrderService.exception;

import com.damu.OrderService.external.response.ErrorResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Log4j2
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException exception) {
            log.warn("Order service exception errorCode={} status={} message={}",
                    exception.getErrorCode(), exception.getStatus(), exception.getMessage());
            return new ResponseEntity<>(ErrorResponse.builder()
                    .errorMessage(exception.getMessage())
                    .errorCode(exception.getErrorCode())
                    .build(),
                    HttpStatus.valueOf(exception.getStatus()));
    }
}
