package com.damu.orderservice.service.impl;

import com.damu.orderservice.entity.Order;
import com.damu.orderservice.exception.CustomException;
import com.damu.orderservice.external.client.PaymentService;
import com.damu.orderservice.external.client.ProductService;
import com.damu.orderservice.external.request.PaymentRequest;
import com.damu.orderservice.external.response.PaymentResponse;
import com.damu.orderservice.external.response.ProductResponse;
import com.damu.orderservice.model.*;
import com.damu.orderservice.repository.OrderRepository;
import com.damu.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Log4j2
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final PaymentService paymentService;

    @Override
    public long placeOrder(OrderRequest orderRequest) {
        log.info("Placing order productId={} quantity={} amount={} paymentMode={}", orderRequest.getProductId(), orderRequest.getQuantity(), orderRequest.getTotalAmount(), orderRequest.getPaymentMode());
        productService.reduceQuantity(orderRequest.getProductId(), orderRequest.getQuantity());
        log.info("Product quantity reduced productId={} quantity={}", orderRequest.getProductId(), orderRequest.getQuantity());
        Order order = orderRepository.save(buildOrder(orderRequest));
        log.info("Order persisted orderId={} status={}", order.getId(), order.getOrderStatus());
        String finalStatus = attemptPayment(order, orderRequest);
        Order finalOrder = orderRepository.save(order.toBuilder().orderStatus(finalStatus).build());
        log.info("Order placement complete orderId={} status={}", finalOrder.getId(), finalOrder.getOrderStatus());
        return finalOrder.getId();
    }

    @Override
    public OrderResponse getOrderDetails(long orderId) {
        log.info("Fetching order details orderId={}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("Order not found for orderId: " + orderId, "NOT_FOUND", 404));
        ProductResponse productResponse = fetchProductDetails(order.getProductId());
        PaymentResponse paymentResponse = fetchPaymentDetails(order.getId());
        OrderResponse orderResponse = buildOrderResponse(order, productResponse, paymentResponse);
        log.info("Order details fetched orderId={} status={}", orderId, orderResponse.getOrderStatus());
        return orderResponse;
    }

    private Order buildOrder(OrderRequest orderRequest) {
        return Order.builder()
                .productId(orderRequest.getProductId())
                .quantity(orderRequest.getQuantity())
                .amount(orderRequest.getTotalAmount())
                .orderStatus("CREATED")
                .orderDate(Instant.now())
                .build();
    }

    private String attemptPayment(Order order, OrderRequest orderRequest) {
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(order.getId())
                .paymentMode(orderRequest.getPaymentMode())
                .amount(orderRequest.getTotalAmount())
                .build();

        try {
            paymentService.doPayment(paymentRequest);
            log.info("Payment successful orderId={}", order.getId());
            return "PLACED";
        } catch (Exception e) {
            log.error("Payment failed orderId={} error={}", order.getId(), e.getMessage(), e);
            return "PAYMENT_FAILED";
        }
    }

    private ProductResponse fetchProductDetails(long productId) {
        return extractData(
                productService.getProduct(productId),
                "Product details unavailable for productId: " + productId
        );
    }

    private PaymentResponse fetchPaymentDetails(long orderId) {
        return extractData(
                paymentService.getPaymentByOrderId(orderId),
                "Payment details unavailable for orderId: " + orderId
        );
    }

    private <T> T extractData(ApiResponse<T> response, String errorMessage) {
        if (response == null || response.getData() == null) {
            throw new CustomException(errorMessage, "DOWNSTREAM_ERROR", 502);
        }
        return response.getData();
    }

    private OrderResponse buildOrderResponse(Order order, ProductResponse product, PaymentResponse payment) {
        return OrderResponse.builder()
                .orderId(order.getId())
                .orderStatus(order.getOrderStatus())
                .amount(order.getAmount())
                .orderDate(order.getOrderDate())
                .productDetails(ProductDetails.builder()
                        .productId(product.getProductId())
                        .productName(product.getProductName())
                        .build())
                .paymentDetails(PaymentDetails.builder()
                        .paymentId(payment.getPaymentId())
                        .paymentStatus(payment.getStatus())
                        .paymentDate(payment.getPaymentDate())
                        .paymentMode(payment.getPaymentMode())
                        .build())
                .build();
    }
}