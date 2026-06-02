package com.damu.ProductService.controller;

import com.damu.ProductService.model.ProductRequest;
import com.damu.ProductService.model.ProductResponse;
import com.damu.ProductService.service.ProductService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
@Log4j2
public class ProductController {

    @Autowired
    private ProductService productService;

    @PreAuthorize("hasAuthority('Admin')")
    @PostMapping
    public ResponseEntity<Long> addProduct(@RequestBody ProductRequest productRequest) {
        log.info("Add product request received name={} quantity={} price={}",
                productRequest.getName(), productRequest.getQuantity(), productRequest.getPrice());
        long productId = productService.addProduct(productRequest);
        log.info("Add product request completed productId={}", productId);
        return new ResponseEntity<>(productId, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('Admin') || hasAuthority('Customer') || hasAuthority('SCOPE_internal')")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable("id") long productId) {
        log.info("Get product request received productId={}", productId);
        ProductResponse productResponse = productService.getProductById(productId);
        log.info("Get product request completed productId={} name={}", productId, productResponse.getProductName());
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @PutMapping("/reduceQuantity/{id}")
    public ResponseEntity<Void> reduceQuantity(@PathVariable("id") long productId, @RequestParam long quantity) {
        log.info("Reduce quantity request received productId={} quantity={}", productId, quantity);
        productService.reduceQuantity(productId, quantity);
        log.info("Reduce quantity request completed productId={} quantity={}", productId, quantity);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
