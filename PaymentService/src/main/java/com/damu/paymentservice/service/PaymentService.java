package com.damu.paymentservice.service;

import com.damu.paymentservice.model.PaymentRequest;
import com.damu.paymentservice.model.PaymentResponse;

public interface PaymentService {
    long doPayment(PaymentRequest paymentRequest);

    PaymentResponse getPaymentDetailsByOrderId(String orderId);
}
