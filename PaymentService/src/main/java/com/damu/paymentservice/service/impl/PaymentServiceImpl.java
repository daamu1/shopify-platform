package com.damu.paymentservice.service.impl;

import com.damu.paymentservice.entity.TransactionDetails;
import com.damu.paymentservice.model.PaymentMode;
import com.damu.paymentservice.model.PaymentRequest;
import com.damu.paymentservice.model.PaymentResponse;
import com.damu.paymentservice.repository.TransactionDetailsRepository;
import com.damu.paymentservice.service.PaymentService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Log4j2
public class PaymentServiceImpl implements PaymentService{

    @Autowired
    private TransactionDetailsRepository transactionDetailsRepository;

    @Override
    public long doPayment(PaymentRequest paymentRequest) {
        log.info("Recording payment orderId={} amount={} paymentMode={}",
                paymentRequest.getOrderId(), paymentRequest.getAmount(), paymentRequest.getPaymentMode());

        TransactionDetails transactionDetails
                = TransactionDetails.builder()
                .paymentDate(Instant.now())
                .paymentMode(paymentRequest.getPaymentMode().name())
                .paymentStatus("SUCCESS")
                .orderId(paymentRequest.getOrderId())
                .referenceNumber(paymentRequest.getReferenceNumber())
                .amount(paymentRequest.getAmount())
                .build();

        transactionDetailsRepository.save(transactionDetails);

        log.info("Payment recorded orderId={} paymentId={} status={}",
                transactionDetails.getOrderId(), transactionDetails.getId(), transactionDetails.getPaymentStatus());

        return transactionDetails.getId();
    }

    @Override
    public PaymentResponse getPaymentDetailsByOrderId(String orderId) {
        log.info("Fetching payment details orderId={}", orderId);

        TransactionDetails transactionDetails
                = transactionDetailsRepository.findByOrderId(Long.parseLong(orderId));

        if (transactionDetails == null) {
            log.warn("Payment details not found orderId={}", orderId);
            throw new IllegalArgumentException("Payment details not found for orderId: " + orderId);
        }

        PaymentResponse paymentResponse = PaymentResponse.builder()
        .paymentId(transactionDetails.getId())
        .paymentMode(PaymentMode.valueOf(transactionDetails.getPaymentMode()))
        .paymentDate(transactionDetails.getPaymentDate())
        .orderId(transactionDetails.getOrderId())
        .status(transactionDetails.getPaymentStatus())
        .amount(transactionDetails.getAmount())
        .build();
        log.info("Payment details fetched orderId={} paymentId={} status={}",
                orderId, paymentResponse.getPaymentId(), paymentResponse.getStatus());
        return paymentResponse;
    }
}
