package com.damu.orderservice.external.client;

import com.damu.orderservice.exception.CustomException;
import com.damu.orderservice.external.request.PaymentRequest;
import com.damu.orderservice.external.response.PaymentResponse;
import com.damu.orderservice.model.ApiResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@CircuitBreaker(name = "external", fallbackMethod = "fallback")
@FeignClient(name = "PAYMENT-SERVICE/payment")
public interface PaymentService {

    @PostMapping
    ApiResponse<Long> doPayment(@RequestBody PaymentRequest paymentRequest);

    @GetMapping("/order/{orderId}")
    ApiResponse<PaymentResponse> getPaymentByOrderId(@PathVariable("orderId") long orderId);

    default ApiResponse<Long> fallback(Exception e) {
        throw new CustomException("Payment Service is not available", "UNAVAILABLE", 500);
    }
}
