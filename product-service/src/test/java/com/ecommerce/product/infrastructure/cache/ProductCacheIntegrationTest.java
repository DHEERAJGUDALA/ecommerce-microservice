package com.ecommerce.product.infrastructure.cache;

import com.ecommerce.product.application.dto.CreateProductRequest;
import com.ecommerce.product.application.dto.ProductDto;
import com.ecommerce.product.application.dto.UpdateProductRequest;
import com.ecommerce.product.application.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.UUID;

import static com.ecommerce.product.infrastructure.config.CacheConfig.PRODUCT_CACHE;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class ProductCacheIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:16-alpine")
    );

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private ProductService productService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        // Clear cache before each test
        cacheManager.getCacheNames().forEach(cacheName ->
            cacheManager.getCache(cacheName).clear()
        );
    }

    @Test
    void shouldCacheProductOnFirstGet() {
        // Given: Create a product
        CreateProductRequest createRequest = new CreateProductRequest(
                "Test Product",
                "Test Description",
                "TEST-SKU-001",
                BigDecimal.valueOf(99.99),
                100,
                null
        );
        ProductDto createdProduct = productService.createProduct(createRequest);
        UUID productId = createdProduct.id();

        // When: Get product for the first time
        ProductDto product1 = productService.getProduct(productId);

        // Then: Product should be cached
        var cache = cacheManager.getCache(PRODUCT_CACHE);
        assertThat(cache).isNotNull();
        assertThat(cache.get(productId)).isNotNull();
        assertThat(cache.get(productId).get()).isInstanceOf(ProductDto.class);

        // When: Get product second time
        ProductDto product2 = productService.getProduct(productId);

        // Then: Should return same cached instance
        assertThat(product1).isEqualTo(product2);
    }

    @Test
    void shouldEvictCacheOnUpdate() {
        // Given: Create and cache a product
        CreateProductRequest createRequest = new CreateProductRequest(
                "Original Product",
                "Original Description",
                "TEST-SKU-002",
                BigDecimal.valueOf(49.99),
                50,
                null
        );
        ProductDto createdProduct = productService.createProduct(createRequest);
        UUID productId = createdProduct.id();

        // Cache the product
        productService.getProduct(productId);
        var cache = cacheManager.getCache(PRODUCT_CACHE);
        assertThat(cache.get(productId)).isNotNull();

        // When: Update the product
        UpdateProductRequest updateRequest = new UpdateProductRequest(
                "Updated Product",
                "Updated Description",
                BigDecimal.valueOf(59.99)
        );
        productService.updateProduct(productId, updateRequest);

        // Then: Cache should be evicted
        assertThat(cache.get(productId)).isNull();

        // When: Get product again
        ProductDto updatedProduct = productService.getProduct(productId);

        // Then: Should have updated values and be cached again
        assertThat(updatedProduct.name()).isEqualTo("Updated Product");
        assertThat(updatedProduct.description()).isEqualTo("Updated Description");
        assertThat(updatedProduct.price()).isEqualByComparingTo(BigDecimal.valueOf(59.99));
        assertThat(cache.get(productId)).isNotNull();
    }

    @Test
    void shouldEvictCacheOnStockUpdate() {
        // Given: Create and cache a product
        CreateProductRequest createRequest = new CreateProductRequest(
                "Stock Test Product",
                "Stock Test Description",
                "TEST-SKU-003",
                BigDecimal.valueOf(29.99),
                100,
                null
        );
        ProductDto createdProduct = productService.createProduct(createRequest);
        UUID productId = createdProduct.id();

        // Cache the product
        ProductDto cachedProduct = productService.getProduct(productId);
        assertThat(cachedProduct.stockQuantity()).isEqualTo(100);

        var cache = cacheManager.getCache(PRODUCT_CACHE);
        assertThat(cache.get(productId)).isNotNull();

        // When: Update stock
        productService.updateStock(productId, 75);

        // Then: Cache should be evicted
        assertThat(cache.get(productId)).isNull();

        // When: Get product again
        ProductDto updatedProduct = productService.getProduct(productId);

        // Then: Should have updated stock and be cached again
        assertThat(updatedProduct.stockQuantity()).isEqualTo(75);
        assertThat(cache.get(productId)).isNotNull();
    }

    @Test
    void shouldEvictAllCachesOnDelete() {
        // Given: Create and cache a product
        CreateProductRequest createRequest = new CreateProductRequest(
                "Delete Test Product",
                "Delete Test Description",
                "TEST-SKU-004",
                BigDecimal.valueOf(19.99),
                25,
                null
        );
        ProductDto createdProduct = productService.createProduct(createRequest);
        UUID productId = createdProduct.id();

        // Cache the product
        productService.getProduct(productId);
        var cache = cacheManager.getCache(PRODUCT_CACHE);
        assertThat(cache.get(productId)).isNotNull();

        // When: Delete the product
        productService.deleteProduct(productId);

        // Then: Cache should be cleared
        assertThat(cache.get(productId)).isNull();
    }

    @Test
    void shouldHandleMultipleProductsInCache() {
        // Given: Create multiple products
        CreateProductRequest request1 = new CreateProductRequest(
                "Product 1", "Description 1", "SKU-001", BigDecimal.valueOf(10.00), 10, null
        );
        CreateProductRequest request2 = new CreateProductRequest(
                "Product 2", "Description 2", "SKU-002", BigDecimal.valueOf(20.00), 20, null
        );
        CreateProductRequest request3 = new CreateProductRequest(
                "Product 3", "Description 3", "SKU-003", BigDecimal.valueOf(30.00), 30, null
        );

        ProductDto product1 = productService.createProduct(request1);
        ProductDto product2 = productService.createProduct(request2);
        ProductDto product3 = productService.createProduct(request3);

        // When: Get all products to cache them
        productService.getProduct(product1.id());
        productService.getProduct(product2.id());
        productService.getProduct(product3.id());

        // Then: All should be cached
        var cache = cacheManager.getCache(PRODUCT_CACHE);
        assertThat(cache.get(product1.id())).isNotNull();
        assertThat(cache.get(product2.id())).isNotNull();
        assertThat(cache.get(product3.id())).isNotNull();

        // When: Update one product
        UpdateProductRequest updateRequest = new UpdateProductRequest(
                "Updated Product 2", "Updated Description 2", BigDecimal.valueOf(25.00)
        );
        productService.updateProduct(product2.id(), updateRequest);

        // Then: Only that product's cache should be evicted
        assertThat(cache.get(product1.id())).isNotNull();
        assertThat(cache.get(product2.id())).isNull();
        assertThat(cache.get(product3.id())).isNotNull();
    }
}
