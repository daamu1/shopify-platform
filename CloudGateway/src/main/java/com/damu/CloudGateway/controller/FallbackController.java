package com.damu.CloudGateway.controller;

import com.damu.CloudGateway.model.ApiResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log4j2
public class FallbackController {

    @GetMapping("/orderServiceFallBack")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ApiResponse<Void> orderServiceFallback() {
        log.warn("Gateway fallback triggered service=ORDER-SERVICE");
        return ApiResponse.fail("Order Service is down", HttpStatus.SERVICE_UNAVAILABLE.value());
    }

    @GetMapping("/paymentServiceFallBack")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ApiResponse<Void> paymentServiceFallback() {
        log.warn("Gateway fallback triggered service=PAYMENT-SERVICE");
        return ApiResponse.fail("Payment Service is down", HttpStatus.SERVICE_UNAVAILABLE.value());
    }

    @GetMapping("/productServiceFallBack")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ApiResponse<Void> productServiceFallback() {
        log.warn("Gateway fallback triggered service=PRODUCT-SERVICE");
        return ApiResponse.fail("Product Service is down", HttpStatus.SERVICE_UNAVAILABLE.value());
    }

    @GetMapping("/userServiceFallBack")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ApiResponse<Void> userServiceFallback() {
        log.warn("Gateway fallback triggered service=USER-SERVICE");
        return ApiResponse.fail("User Service is down", HttpStatus.SERVICE_UNAVAILABLE.value());
    }

}
