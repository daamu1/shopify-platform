package com.damu.OrderService.service;

import com.damu.OrderService.model.OrderRequest;
import com.damu.OrderService.model.OrderResponse;

public interface OrderService {
    long placeOrder(OrderRequest orderRequest);

    OrderResponse getOrderDetails(long orderId);
}
