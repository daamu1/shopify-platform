package com.damu.orderservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private long id;

    @Column(name = "product_id", nullable = false)
    private long productId;

    @Column(name = "quantity", nullable = false)
    private long quantity;

    @Column(name = "order_date", nullable = false)
    private Instant orderDate;

    @Column(name = "order_status", nullable = false, length = 30)
    private String orderStatus;

    @Column(name = "total_amount", nullable = false)
    private long amount;
}
