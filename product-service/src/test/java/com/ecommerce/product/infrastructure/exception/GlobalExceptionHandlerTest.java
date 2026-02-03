package com.ecommerce.product.infrastructure.exception;

import com.ecommerce.product.application.dto.CreateProductRequest;
import com.ecommerce.product.application.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class GlobalExceptionHandlerTest {

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
    private MockMvc mockMvc;

    @Autowired
    private ProductService productService;

    @Test
    void shouldHandleResourceNotFoundException() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/products/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.message").value(containsString("Product not found")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldHandleValidationErrorsForMissingRequiredFields() throws Exception {
        // Given: Invalid request with missing required fields
        String invalidJson = """
                {
                    "description": "Test Description"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.validationErrors").isMap())
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.sku").exists())
                .andExpect(jsonPath("$.validationErrors.price").exists())
                .andExpect(jsonPath("$.validationErrors.stockQuantity").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldHandleValidationErrorsForInvalidValues() throws Exception {
        // Given: Invalid request with negative values
        String invalidJson = """
                {
                    "name": "",
                    "description": "Test Description",
                    "sku": "TEST-SKU",
                    "price": -10.00,
                    "stockQuantity": -5
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.validationErrors").isMap())
                .andExpect(jsonPath("$.validationErrors.name").value(containsString("required")))
                .andExpect(jsonPath("$.validationErrors.price").value(containsString("greater than 0")))
                .andExpect(jsonPath("$.validationErrors.stockQuantity").value(containsString("cannot be negative")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldHandleBusinessExceptionForDuplicateSku() throws Exception {
        // Given: Create a product first
        CreateProductRequest firstRequest = new CreateProductRequest(
                "First Product",
                "Description",
                "DUPLICATE-SKU",
                BigDecimal.valueOf(99.99),
                100,
                null
        );
        productService.createProduct(firstRequest);

        // When: Try to create another product with same SKU
        String duplicateJson = """
                {
                    "name": "Second Product",
                    "description": "Different Description",
                    "sku": "DUPLICATE-SKU",
                    "price": 49.99,
                    "stockQuantity": 50
                }
                """;

        // Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("SKU_EXISTS"))
                .andExpect(jsonPath("$.message").value("Product with this SKU already exists"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldHandleIllegalArgumentException() throws Exception {
        // Given: Create a product first
        CreateProductRequest request = new CreateProductRequest(
                "Test Product",
                "Description",
                "TEST-ILLEGAL-SKU",
                BigDecimal.valueOf(99.99),
                100,
                null
        );
        var product = productService.createProduct(request);

        // When: Try to update stock with negative value
        mockMvc.perform(patch("/api/products/{id}/stock", product.id())
                        .param("quantity", "-10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Invalid Argument"))
                .andExpect(jsonPath("$.message").value("Stock quantity cannot be negative"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturnValidResponseForValidRequest() throws Exception {
        // Given: Valid request
        String validJson = """
                {
                    "name": "Valid Product",
                    "description": "Valid Description",
                    "sku": "VALID-SKU-123",
                    "price": 99.99,
                    "stockQuantity": 100
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Valid Product"))
                .andExpect(jsonPath("$.description").value("Valid Description"))
                .andExpect(jsonPath("$.sku").value("VALID-SKU-123"))
                .andExpect(jsonPath("$.price").value(99.99))
                .andExpect(jsonPath("$.stockQuantity").value(100));
    }
}
