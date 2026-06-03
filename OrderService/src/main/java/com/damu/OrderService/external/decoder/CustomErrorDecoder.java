package com.damu.OrderService.external.decoder;

import com.damu.OrderService.exception.CustomException;
import com.damu.OrderService.model.ApiResponse;
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
            ApiResponse<?> apiResponse = objectMapper.readValue(response.body().asInputStream(), ApiResponse.class);
            String errorCode = apiResponse.getErrors() == null || apiResponse.getErrors().isEmpty()
                    ? "DOWNSTREAM_ERROR"
                    : apiResponse.getErrors().get(0).getReason();
            log.warn("Feign error decoded errorCode={} message={}", errorCode, apiResponse.getMessage());
            return new CustomException(apiResponse.getMessage(), errorCode, response.status());

        } catch (IOException e) {
            log.error("Unable to decode Feign error response clientMethod={} status={}", s, response.status(), e);
            throw new CustomException("Internal Server Error", "INTERNAL_SERVER_ERROR", 500);
        }
    }
}
