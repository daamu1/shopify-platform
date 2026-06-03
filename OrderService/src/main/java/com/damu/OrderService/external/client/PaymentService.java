package com.damu.OrderService.external.client;

import com.damu.OrderService.exception.CustomException;
import com.damu.OrderService.external.request.PaymentRequest;
import com.damu.OrderService.model.ApiResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@CircuitBreaker(name = "external", fallbackMethod = "fallback")
@FeignClient(name = "PAYMENT-SERVICE/payment")
public interface PaymentService {

    @PostMapping
    ApiResponse<Long> doPayment(@RequestBody PaymentRequest paymentRequest);

    default ApiResponse<Long> fallback(Exception e) {
        throw new CustomException("Payment Service is not available", "UNAVAILABLE", 500);
    }
}
