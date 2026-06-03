package com.damu.ProductService.service.impl;

import com.damu.ProductService.entity.Product;
import com.damu.ProductService.exception.ProductServiceCustomException;
import com.damu.ProductService.model.ProductRequest;
import com.damu.ProductService.model.ProductResponse;
import com.damu.ProductService.repository.ProductRepository;
import com.damu.ProductService.service.ProductService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.springframework.beans.BeanUtils.*;

@Service
@Log4j2
public class ProductServiceImpl implements ProductService{

    @Autowired
    private ProductRepository productRepository;

    @Override
    public long addProduct(ProductRequest productRequest) {
       log.info("Creating product name={} quantity={} price={}",
               productRequest.getName(), productRequest.getQuantity(), productRequest.getPrice());

        Product product
                = Product.builder()
                .productName(productRequest.getName())
                .quantity(productRequest.getQuantity())
                .price(productRequest.getPrice())
                .build();

        productRepository.save(product);

        log.info("Product created productId={} name={}", product.getProductId(), product.getProductName());
        return product.getProductId();
    }

    @Override
    public ProductResponse getProductById(long productId) {
        log.info("Fetching product productId={}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product not found productId={}", productId);
                    return new ProductServiceCustomException("Product with given id not found","PRODUCT_NOT_FOUND");
                });

        ProductResponse productResponse = new ProductResponse();
        copyProperties(product, productResponse);
        log.info("Product fetched productId={} quantity={} price={}", productId, product.getQuantity(), product.getPrice());
        return productResponse;
    }

    @Override
    public void reduceQuantity(long productId, long quantity) {
        log.info("Reducing product quantity productId={} requestedQuantity={}", productId, quantity);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product not found while reducing quantity productId={}", productId);
                    return new ProductServiceCustomException("Product with given Id not found", "PRODUCT_NOT_FOUND");
                });

        if(product.getQuantity() < quantity) {
            log.warn("Insufficient product quantity productId={} availableQuantity={} requestedQuantity={}",
                    productId, product.getQuantity(), quantity);
            throw new ProductServiceCustomException("Product does not have sufficient Quantity", "INSUFFICIENT_QUANTITY");
        }

        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);
        log.info("Product quantity updated productId={} remainingQuantity={}", productId, product.getQuantity());
    }
}
