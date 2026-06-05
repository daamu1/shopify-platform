package com.damu.productservice.controller;

import com.damu.productservice.model.ApiResponse;
import com.damu.productservice.model.ProductRequest;
import com.damu.productservice.model.ProductResponse;
import com.damu.productservice.service.ProductService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
@Log4j2
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('product:create')")
    public ApiResponse<Long> addProduct(@RequestBody ProductRequest productRequest) {
        log.info("Add product request received name={} quantity={} price={}", productRequest.getName(), productRequest.getQuantity(), productRequest.getPrice());
        long productId = productService.addProduct(productRequest);
        log.info("Add product request completed productId={}", productId);
        return ApiResponse.ok("Product created successfully", HttpStatus.CREATED.value(), productId);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('product:read')")
    public ApiResponse<ProductResponse> getProductById(@PathVariable("id") long productId) {
        log.info("Get product request received productId={}", productId);
        ProductResponse productResponse = productService.getProductById(productId);
        log.info("Get product request completed productId={} name={}", productId, productResponse.getProductName());
        return ApiResponse.ok("Product fetched successfully", HttpStatus.OK.value(), productResponse);
    }

    @PutMapping("/reduceQuantity/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> reduceQuantity(@PathVariable("id") long productId, @RequestParam long quantity) {
        log.info("Reduce quantity request received productId={} quantity={}", productId, quantity);
        productService.reduceQuantity(productId, quantity);
        log.info("Reduce quantity request completed productId={} quantity={}", productId, quantity);
        return ApiResponse.ok("Product quantity reduced successfully", HttpStatus.OK.value());
    }
}
