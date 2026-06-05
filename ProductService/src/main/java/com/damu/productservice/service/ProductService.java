package com.damu.productservice.service;

import com.damu.productservice.model.ProductRequest;
import com.damu.productservice.model.ProductResponse;

public interface ProductService {
    long addProduct(ProductRequest productRequest);

    ProductResponse getProductById(long productId);

    void reduceQuantity(long productId, long quantity);
}
