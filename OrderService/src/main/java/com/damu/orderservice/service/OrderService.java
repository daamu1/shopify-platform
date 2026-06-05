package com.damu.orderservice.service;

import com.damu.orderservice.model.OrderRequest;
import com.damu.orderservice.model.OrderResponse;

public interface OrderService {
    long placeOrder(OrderRequest orderRequest);

    OrderResponse getOrderDetails(long orderId);
}
