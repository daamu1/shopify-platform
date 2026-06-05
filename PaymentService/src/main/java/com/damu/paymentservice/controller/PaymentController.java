package com.damu.paymentservice.controller;

import com.damu.paymentservice.model.ApiResponse;
import com.damu.paymentservice.model.PaymentRequest;
import com.damu.paymentservice.model.PaymentResponse;
import com.damu.paymentservice.service.PaymentService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
@Log4j2
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Long> doPayment(@RequestBody PaymentRequest paymentRequest) {
        log.info("Payment request received orderId={} amount={} paymentMode={}",
                paymentRequest.getOrderId(), paymentRequest.getAmount(), paymentRequest.getPaymentMode());
        long paymentId = paymentService.doPayment(paymentRequest);
        log.info("Payment request completed orderId={} paymentId={}", paymentRequest.getOrderId(), paymentId);
        return ApiResponse.ok("Payment completed successfully", HttpStatus.OK.value(), paymentId);
    }

    @GetMapping("/order/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PaymentResponse> getPaymentDetailsByOrderId(@PathVariable String orderId) {
        log.info("Get payment details request received orderId={}", orderId);
        PaymentResponse paymentResponse = paymentService.getPaymentDetailsByOrderId(orderId);
        log.info("Get payment details request completed orderId={} paymentId={} status={}",
                orderId, paymentResponse.getPaymentId(), paymentResponse.getStatus());
        return ApiResponse.ok("Payment details fetched successfully", HttpStatus.OK.value(), paymentResponse);
    }

}
