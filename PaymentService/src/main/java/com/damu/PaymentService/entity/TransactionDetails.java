package com.damu.PaymentService.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "transaction_details")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private long id;

    @Column(name = "order_id", nullable = false)
    private long orderId;

    @Column(name = "payment_mode", nullable = false, length = 30)
    private String paymentMode;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "payment_date", nullable = false)
    private Instant paymentDate;

    @Column(name = "payment_status", nullable = false, length = 30)
    private String paymentStatus;

    @Column(name = "amount", nullable = false)
    private long amount;
}
