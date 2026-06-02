package com.damu.CloudGateway.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log4j2
public class FallbackController {

    @GetMapping("/orderServiceFallBack")
    public String orderServiceFallback() {
        log.warn("Gateway fallback triggered service=ORDER-SERVICE");
        return "Order Service is down!";
    }

    @GetMapping("/paymentServiceFallBack")
    public String paymentServiceFallback() {
        log.warn("Gateway fallback triggered service=PAYMENT-SERVICE");
        return "Payment Service is down!";
    }

    @GetMapping("/productServiceFallBack")
    public String productServiceFallback() {
        log.warn("Gateway fallback triggered service=PRODUCT-SERVICE");
        return "Product Service is down!";
    }

}
