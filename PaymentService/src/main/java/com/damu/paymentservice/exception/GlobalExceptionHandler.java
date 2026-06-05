package com.damu.paymentservice.exception;

import com.damu.paymentservice.model.ApiResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalArgumentException(IllegalArgumentException exception) {
        log.warn("Payment service validation exception message={}", exception.getMessage());
        return ApiResponse.fail(exception.getMessage(), HttpStatus.BAD_REQUEST.value(),
                List.of(ApiResponse.ApiError.of("INVALID_REQUEST")));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception exception) {
        log.error("Payment service unexpected exception message={}", exception.getMessage(), exception);
        return ApiResponse.fail("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                List.of(ApiResponse.ApiError.of("INTERNAL_SERVER_ERROR")));
    }
}
