package com.damu.OrderService.controller;

import com.damu.OrderService.model.OrderRequest;
import com.damu.OrderService.model.OrderResponse;
import com.damu.OrderService.service.OrderService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@Log4j2
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PreAuthorize("hasAuthority('Customer')")
    @PostMapping("/placeOrder")
    public ResponseEntity<Long> placeOrder(@RequestBody OrderRequest orderRequest) {
        log.info("Place order request received productId={} quantity={} amount={}",
                orderRequest.getProductId(), orderRequest.getQuantity(), orderRequest.getTotalAmount());
        long orderId = orderService.placeOrder(orderRequest);
        log.info("Place order request completed orderId={}", orderId);
        return new ResponseEntity<>(orderId, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('Admin') || hasAuthority('Customer')")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderDetails(@PathVariable long orderId) {
        log.info("Get order details request received orderId={}", orderId);
        OrderResponse orderResponse = orderService.getOrderDetails(orderId);
        log.info("Get order details request completed orderId={} status={}", orderId, orderResponse.getOrderStatus());
        return new ResponseEntity<>(orderResponse, HttpStatus.OK);
    }
}
