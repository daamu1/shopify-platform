package com.damu.ProductService.exception;

import com.damu.ProductService.model.ApiResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductServiceCustomException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleProductServiceException(ProductServiceCustomException exception) {
        log.warn("Product service exception errorCode={} message={}", exception.getErrorCode(), exception.getMessage());
        return ApiResponse.fail(exception.getMessage(), HttpStatus.NOT_FOUND.value(),
                List.of(ApiResponse.ApiError.of(exception.getErrorCode())));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception exception) {
        log.error("Product service unexpected exception message={}", exception.getMessage(), exception);
        return ApiResponse.fail("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                List.of(ApiResponse.ApiError.of("INTERNAL_SERVER_ERROR")));
    }
}
