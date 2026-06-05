package com.damu.orderservice.external.client;

import com.damu.orderservice.exception.CustomException;
import com.damu.orderservice.model.ApiResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@CircuitBreaker(name = "external", fallbackMethod = "fallback")
@FeignClient(name = "PRODUCT-SERVICE/product")
public interface ProductService {

    @PutMapping("/reduceQuantity/{id}")
    ApiResponse<Void> reduceQuantity(@PathVariable("id") long productId, @RequestParam long quantity);


    default ApiResponse<Void> fallback(Exception e) {
        throw new CustomException("Product Service is not available", "UNAVAILABLE", 500);
    }

}
