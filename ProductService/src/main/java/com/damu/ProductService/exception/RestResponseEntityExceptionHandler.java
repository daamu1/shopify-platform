package com.damu.ProductService.exception;

import com.damu.ProductService.model.ErrorResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Log4j2
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ProductServiceCustomException.class)
    public ResponseEntity<ErrorResponse> handleProductServiceException(ProductServiceCustomException exception) {
        log.warn("Product service exception errorCode={} message={}", exception.getErrorCode(), exception.getMessage());
        return new ResponseEntity<>(ErrorResponse.builder()
                    .errorMessage(exception.getMessage())
                    .errorCode(exception.getErrorCode())
                    .build(), HttpStatus.NOT_FOUND);
    }
}
