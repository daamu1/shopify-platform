package com.damu.PaymentService.controller;

import com.damu.PaymentService.model.PaymentRequest;
import com.damu.PaymentService.model.PaymentResponse;
import com.damu.PaymentService.service.PaymentService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
@Log4j2
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Long> doPayment(@RequestBody PaymentRequest paymentRequest) {
        log.info("Payment request received orderId={} amount={} paymentMode={}",
                paymentRequest.getOrderId(), paymentRequest.getAmount(), paymentRequest.getPaymentMode());
        long paymentId = paymentService.doPayment(paymentRequest);
        log.info("Payment request completed orderId={} paymentId={}", paymentRequest.getOrderId(), paymentId);
        return new ResponseEntity<>(
                paymentId,
                HttpStatus.OK
        );
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentDetailsByOrderId(@PathVariable String orderId) {
        log.info("Get payment details request received orderId={}", orderId);
        PaymentResponse paymentResponse = paymentService.getPaymentDetailsByOrderId(orderId);
        log.info("Get payment details request completed orderId={} paymentId={} status={}",
                orderId, paymentResponse.getPaymentId(), paymentResponse.getStatus());
        return new ResponseEntity<>(
                paymentResponse,
                HttpStatus.OK
        );
    }

}
