package com.ecommerce.product.infrastructure.persistence;

import com.ecommerce.product.domain.entity.Product;
import com.ecommerce.product.domain.repository.ProductRepository;
import com.ecommerce.product.domain.valueobject.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:16-alpine")
    );

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldSaveAndFindProduct() {
        // Given
        Product product = Product.create(
                "Test Product",
                "Test Description",
                "TEST-SKU",
                Money.of(BigDecimal.valueOf(99.99)),
                100,
                null
        );

        // When
        Product saved = productRepository.save(product);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Test Product");
        assertThat(saved.getDescription()).isEqualTo("Test Description");
        assertThat(saved.getSku()).isEqualTo("TEST-SKU");
        assertThat(saved.getPrice().amount()).isEqualByComparingTo(BigDecimal.valueOf(99.99));
        assertThat(saved.getStockQuantity()).isEqualTo(100);
    }

    @Test
    void shouldFindProductById() {
        // Given
        Product product = Product.create(
                "Find Test",
                "Description",
                "FIND-SKU",
                Money.of(BigDecimal.valueOf(49.99)),
                50,
                null
        );
        product = productRepository.save(product);

        // When
        Optional<Product> found = productRepository.findById(product.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Find Test");
    }

    @Test
    void shouldReturnEmptyWhenProductNotFound() {
        // When
        Optional<Product> found = productRepository.findById(UUID.randomUUID());

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldCheckIfProductExistsBySku() {
        // Given
        Product product = Product.create(
                "SKU Test",
                "Description",
                "UNIQUE-SKU",
                Money.of(BigDecimal.valueOf(29.99)),
                30,
                null
        );
        productRepository.save(product);

        // When & Then
        assertThat(productRepository.existsBySku("UNIQUE-SKU")).isTrue();
        assertThat(productRepository.existsBySku("NON-EXISTENT-SKU")).isFalse();
    }

    @Test
    void shouldFindAllProductsWithPagination() {
        // Given: Create multiple products
        for (int i = 1; i <= 5; i++) {
            Product product = Product.create(
                    "Product " + i,
                    "Description " + i,
                    "SKU-" + i,
                    Money.of(BigDecimal.valueOf(10.00 * i)),
                    10 * i,
                    null
            );
            productRepository.save(product);
        }

        // When
        Page<Product> page = productRepository.findAll(PageRequest.of(0, 3));

        // Then
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(5);
        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalPages()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldFindProductsByCategory() {
        // Given
        UUID categoryId = UUID.randomUUID();

        Product product1 = Product.create(
                "Category Product 1",
                "Description 1",
                "CAT-SKU-1",
                Money.of(BigDecimal.valueOf(19.99)),
                10,
                categoryId
        );

        Product product2 = Product.create(
                "Category Product 2",
                "Description 2",
                "CAT-SKU-2",
                Money.of(BigDecimal.valueOf(29.99)),
                20,
                categoryId
        );

        Product product3 = Product.create(
                "Other Product",
                "Description 3",
                "OTHER-SKU",
                Money.of(BigDecimal.valueOf(39.99)),
                30,
                UUID.randomUUID()
        );

        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);

        // When
        Page<Product> categoryProducts = productRepository.findByCategoryId(categoryId, PageRequest.of(0, 10));

        // Then
        assertThat(categoryProducts.getContent()).hasSize(2);
        assertThat(categoryProducts.getContent())
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Category Product 1", "Category Product 2");
    }

    @Test
    void shouldUpdateProduct() {
        // Given
        Product product = Product.create(
                "Original Name",
                "Original Description",
                "UPDATE-SKU",
                Money.of(BigDecimal.valueOf(99.99)),
                100,
                null
        );
        product = productRepository.save(product);

        // When
        product.updateDetails(
                "Updated Name",
                "Updated Description",
                Money.of(BigDecimal.valueOf(79.99))
        );
        product = productRepository.save(product);

        // Then
        Product updated = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getDescription()).isEqualTo("Updated Description");
        assertThat(updated.getPrice().amount()).isEqualByComparingTo(BigDecimal.valueOf(79.99));
    }

    @Test
    void shouldDeleteProduct() {
        // Given
        Product product = Product.create(
                "Delete Test",
                "Description",
                "DELETE-SKU",
                Money.of(BigDecimal.valueOf(19.99)),
                25,
                null
        );
        product = productRepository.save(product);
        UUID productId = product.getId();

        // When
        productRepository.delete(product);

        // Then
        assertThat(productRepository.findById(productId)).isEmpty();
    }

    @Test
    void shouldUpdateStockQuantity() {
        // Given
        Product product = Product.create(
                "Stock Test",
                "Description",
                "STOCK-SKU",
                Money.of(BigDecimal.valueOf(49.99)),
                100,
                null
        );
        product = productRepository.save(product);

        // When
        product.updateStock(75);
        productRepository.save(product);

        // Then
        Product updated = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updated.getStockQuantity()).isEqualTo(75);
    }
}
