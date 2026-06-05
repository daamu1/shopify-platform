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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
@Log4j2
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService{
    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final PaymentService paymentService;
    private final RestTemplate restTemplate;

    @Override
    public long placeOrder(OrderRequest orderRequest) {
        log.info("Starting order placement productId={} quantity={} amount={} paymentMode={}", orderRequest.getProductId(), orderRequest.getQuantity(), orderRequest.getTotalAmount(), orderRequest.getPaymentMode());
        log.info("Reducing product quantity productId={} quantity={}", orderRequest.getProductId(), orderRequest.getQuantity());
        productService.reduceQuantity(orderRequest.getProductId(), orderRequest.getQuantity());
        log.info("Product quantity reduced productId={} quantity={}", orderRequest.getProductId(), orderRequest.getQuantity());
        log.info("Creating order with status=CREATED productId={}", orderRequest.getProductId());
        Order order = Order.builder()
                .amount(orderRequest.getTotalAmount())
                .orderStatus("CREATED")
                .productId(orderRequest.getProductId())
                .orderDate(Instant.now())
                .quantity(orderRequest.getQuantity())
                .build();

        order = orderRepository.save(order);
        log.info("Order persisted orderId={} status={}", order.getId(), order.getOrderStatus());
        log.info("Calling payment service orderId={} amount={}", order.getId(), orderRequest.getTotalAmount());
        PaymentRequest paymentRequest
                = PaymentRequest.builder()
                .orderId(order.getId())
                .paymentMode(orderRequest.getPaymentMode())
                .amount(orderRequest.getTotalAmount())
                .build();

        String orderStatus = null;
        try {
            paymentService.doPayment(paymentRequest);
            log.info("Payment completed successfully orderId={}", order.getId());
            orderStatus = "PLACED";
        } catch (Exception e) {
            log.error("Payment failed orderId={} nextStatus=PAYMENT_FAILED error={}", order.getId(), e.getMessage(), e);
            orderStatus = "PAYMENT_FAILED";
        }

        order.setOrderStatus(orderStatus);
        orderRepository.save(order);

        log.info("Order placement completed orderId={} finalStatus={}", order.getId(), orderStatus);
        return order.getId();
    }

    @Override
    public OrderResponse getOrderDetails(long orderId) {
        log.info("Fetching order details orderId={}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Order not found orderId={}", orderId);
                    return new CustomException("Order not found for the order Id:" + orderId, "NOT_FOUND", 404);
                });

        log.info("Calling product service for order details orderId={} productId={}", orderId, order.getProductId());
        ApiResponse<ProductResponse> productApiResponse = restTemplate.exchange(
                "http://PRODUCT-SERVICE/product/" + order.getProductId(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponse<ProductResponse>>() {
                }).getBody();
        if (productApiResponse == null || productApiResponse.getData() == null) {
            throw new CustomException("Product details are not available", "DOWNSTREAM_ERROR", 502);
        }
        ProductResponse productResponse = productApiResponse.getData();

        log.info("Calling payment service for order details orderId={}", orderId);
        ApiResponse<PaymentResponse> paymentApiResponse = restTemplate.exchange(
                "http://PAYMENT-SERVICE/payment/order/" + order.getId(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponse<PaymentResponse>>() {
                }).getBody();
        if (paymentApiResponse == null || paymentApiResponse.getData() == null) {
            throw new CustomException("Payment details are not available", "DOWNSTREAM_ERROR", 502);
        }
        PaymentResponse paymentResponse = paymentApiResponse.getData();

      ProductDetails productDetails
                = ProductDetails
                .builder()
                .productName(productResponse.getProductName())
                .productId(productResponse.getProductId())
                .build();

       PaymentDetails paymentDetails
                = PaymentDetails
                .builder()
                .paymentId(paymentResponse.getPaymentId())
                .paymentStatus(paymentResponse.getStatus())
                .paymentDate(paymentResponse.getPaymentDate())
                .paymentMode(paymentResponse.getPaymentMode())
                .build();

        OrderResponse orderResponse
                = OrderResponse.builder()
                .orderId(order.getId())
                .orderStatus(order.getOrderStatus())
                .amount(order.getAmount())
                .orderDate(order.getOrderDate())
                .productDetails(productDetails)
                .paymentDetails(paymentDetails)
                .build();

        log.info("Order details fetched orderId={} status={}", orderId, orderResponse.getOrderStatus());
        return orderResponse;
    }
}
