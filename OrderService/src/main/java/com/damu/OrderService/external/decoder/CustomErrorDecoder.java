package com.damu.OrderService.external.decoder;

import com.damu.OrderService.exception.CustomException;
import com.damu.OrderService.external.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
public class CustomErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String s, Response response) {
        ObjectMapper objectMapper = new ObjectMapper();
        log.warn("Feign call failed clientMethod={} status={} url={}", s, response.status(), response.request().url());
        try {
            ErrorResponse errorResponse = objectMapper.readValue(response.body().asInputStream(), ErrorResponse.class);
            log.warn("Feign error decoded errorCode={} message={}", errorResponse.getErrorCode(), errorResponse.getErrorMessage());
            return new CustomException(errorResponse.getErrorMessage(), errorResponse.getErrorCode(), response.status());

        } catch (IOException e) {
            log.error("Unable to decode Feign error response clientMethod={} status={}", s, response.status(), e);
            throw new CustomException("Internal Server Error", "INTERNAL_SERVER_ERROR", 500);
        }
    }
}
