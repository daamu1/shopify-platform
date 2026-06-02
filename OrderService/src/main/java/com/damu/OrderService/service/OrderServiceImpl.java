package com.damu.OrderService.service;

import com.damu.OrderService.entity.Order;
import com.damu.OrderService.exception.CustomException;
import com.damu.OrderService.external.client.PaymentService;
import com.damu.OrderService.external.client.ProductService;
import com.damu.OrderService.external.request.PaymentRequest;
import com.damu.OrderService.external.response.PaymentResponse;
import com.damu.OrderService.external.response.ProductResponse;
import com.damu.OrderService.model.OrderRequest;
import com.damu.OrderService.model.OrderResponse;
import com.damu.OrderService.repository.OrderRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Service
@Log4j2
public class OrderServiceImpl implements OrderService{

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public long placeOrder(OrderRequest orderRequest) {

        //Order Entity -> Save the data with Status Order Created
        //Product Service - Block Products (Reduce the Quantity)
        //Payment Service -> Payments -> Success-> COMPLETE, Else
        //CANCELLED

        log.info("Starting order placement productId={} quantity={} amount={} paymentMode={}",
                orderRequest.getProductId(), orderRequest.getQuantity(), orderRequest.getTotalAmount(), orderRequest.getPaymentMode());

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
        ProductResponse productResponse = restTemplate.getForObject("http://PRODUCT-SERVICE/product/" + order.getProductId(), ProductResponse.class);

        log.info("Calling payment service for order details orderId={}", orderId);
        PaymentResponse paymentResponse
                = restTemplate.getForObject("http://PAYMENT-SERVICE/payment/order/" + order.getId(), PaymentResponse.class);

        OrderResponse.ProductDetails productDetails
                = OrderResponse.ProductDetails
                .builder()
                .productName(productResponse.getProductName())
                .productId(productResponse.getProductId())
                .build();

        OrderResponse.PaymentDetails paymentDetails
                = OrderResponse.PaymentDetails
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
