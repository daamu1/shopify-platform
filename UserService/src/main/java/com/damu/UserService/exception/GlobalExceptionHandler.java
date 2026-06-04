package com.damu.UserService.exception;

import com.damu.UserService.model.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserServiceException.class)
    public ApiResponse<Void> handleUserServiceException(UserServiceException exception, HttpServletResponse response) {
        log.warn("User service exception errorCode={} status={} message={}", exception.getErrorCode(), exception.getStatus(), exception.getMessage());
        HttpStatus status = HttpStatus.valueOf(exception.getStatus());
        response.setStatus(status.value());
        return ApiResponse.fail(exception.getMessage(), status.value(), List.of(ApiResponse.ApiError.of(exception.getErrorCode())));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ApiResponse<Void> handleResponseStatusException(ResponseStatusException exception, HttpServletResponse response) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        response.setStatus(status.value());
        return ApiResponse.fail(exception.getReason(), status.value());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception exception) {
        log.error("User service unexpected exception message={}", exception.getMessage(), exception);
        return ApiResponse.fail("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value(), List.of(ApiResponse.ApiError.of("INTERNAL_SERVER_ERROR")));
    }
}
