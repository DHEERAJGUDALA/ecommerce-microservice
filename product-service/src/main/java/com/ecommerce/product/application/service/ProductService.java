package com.ecommerce.product.application.service;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.product.application.dto.*;
import com.ecommerce.product.domain.entity.Product;
import com.ecommerce.product.domain.event.ProductCreatedEvent;
import com.ecommerce.product.domain.repository.ProductRepository;
import com.ecommerce.product.domain.valueobject.Money;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.ecommerce.product.infrastructure.config.CacheConfig.PRODUCT_CACHE;
import static com.ecommerce.product.infrastructure.config.CacheConfig.PRODUCT_LIST_CACHE;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ProductService(ProductRepository productRepository, ApplicationEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }

    @CacheEvict(value = {PRODUCT_CACHE, PRODUCT_LIST_CACHE}, allEntries = true)
    public ProductDto createProduct(CreateProductRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new BusinessException("SKU_EXISTS", "Product with this SKU already exists");
        }

        Money price = Money.of(request.price());
        Product product = Product.create(
                request.name(),
                request.description(),
                request.sku(),
                price,
                request.stockQuantity(),
                request.categoryId()
        );

        product = productRepository.save(product);

        eventPublisher.publishEvent(new ProductCreatedEvent(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getPrice().amount(),
                product.getStockQuantity()
        ));

        return toDto(product);
    }

    @Cacheable(value = PRODUCT_CACHE, key = "#id")
    @Transactional(readOnly = true)
    public ProductDto getProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return toDto(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> getProductsByCategory(UUID categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable).map(this::toDto);
    }

    @CacheEvict(value = PRODUCT_CACHE, key = "#id")
    public ProductDto updateProduct(UUID id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        Money price = Money.of(request.price());
        product.updateDetails(request.name(), request.description(), price);

        product = productRepository.save(product);
        return toDto(product);
    }

    @CacheEvict(value = PRODUCT_CACHE, key = "#id")
    public void updateStock(UUID id, int quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        product.updateStock(quantity);
        productRepository.save(product);
    }

    public void reserveStock(UUID id, int quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        product.reserveStock(quantity);
        productRepository.save(product);
    }

    public void releaseStock(UUID id, int quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        product.releaseStock(quantity);
        productRepository.save(product);
    }

    @CacheEvict(value = {PRODUCT_CACHE, PRODUCT_LIST_CACHE}, allEntries = true)
    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        productRepository.delete(product);
    }

    private ProductDto toDto(Product product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getSku(),
                product.getPrice().amount(),
                product.getPrice().currency().getCurrencyCode(),
                product.getStockQuantity(),
                product.getCategoryId(),
                product.getStatus().name(),
                product.isAvailable(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
