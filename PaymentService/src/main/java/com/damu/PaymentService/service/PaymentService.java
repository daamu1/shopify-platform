package com.damu.PaymentService.service;

import com.damu.PaymentService.model.PaymentRequest;
import com.damu.PaymentService.model.PaymentResponse;

public interface PaymentService {
    long doPayment(PaymentRequest paymentRequest);

    PaymentResponse getPaymentDetailsByOrderId(String orderId);
}
