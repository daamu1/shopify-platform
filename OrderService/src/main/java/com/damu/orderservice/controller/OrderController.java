package com.damu.orderservice.controller;

import com.damu.orderservice.model.ApiResponse;
import com.damu.orderservice.model.OrderRequest;
import com.damu.orderservice.model.OrderResponse;
import com.damu.orderservice.service.OrderService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@Log4j2
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PreAuthorize("hasAuthority('order:create')")
    @PostMapping("/placeOrder")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Long> placeOrder(@RequestBody OrderRequest orderRequest) {
        log.info("Place order request received productId={} quantity={} amount={}",
                orderRequest.getProductId(), orderRequest.getQuantity(), orderRequest.getTotalAmount());
        long orderId = orderService.placeOrder(orderRequest);
        log.info("Place order request completed orderId={}", orderId);
        return ApiResponse.ok("Order placed successfully", HttpStatus.OK.value(), orderId);
    }

    @PreAuthorize("hasAuthority('order:read:self') || hasAuthority('order:read:any')")
    @GetMapping("/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<OrderResponse> getOrderDetails(@PathVariable long orderId) {
        log.info("Get order details request received orderId={}", orderId);
        OrderResponse orderResponse = orderService.getOrderDetails(orderId);
        log.info("Get order details request completed orderId={} status={}", orderId, orderResponse.getOrderStatus());
        return ApiResponse.ok("Order details fetched successfully", HttpStatus.OK.value(), orderResponse);
    }
}
