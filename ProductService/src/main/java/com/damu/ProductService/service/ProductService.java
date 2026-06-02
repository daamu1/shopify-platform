package com.damu.ProductService.service;

import com.damu.ProductService.model.ProductRequest;
import com.damu.ProductService.model.ProductResponse;

public interface ProductService {
    long addProduct(ProductRequest productRequest);

    ProductResponse getProductById(long productId);

    void reduceQuantity(long productId, long quantity);
}
