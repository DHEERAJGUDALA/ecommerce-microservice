# Product Service - Production-Grade Implementation

## Overview

The Product Service implements a production-grade Product Catalog with comprehensive caching, validation, exception handling, and testing following Domain-Driven Design (DDD) principles.

## Features Implemented

### 1. Domain Entity - Product

**Location:** `domain/entity/Product.java`

The Product entity includes:
- **id**: UUID (auto-generated)
- **name**: String
- **description**: String
- **sku**: String (unique)
- **price**: Money value object (BigDecimal + Currency)
- **stockQuantity**: Integer
- **categoryId**: UUID (optional)
- **status**: ProductStatus enum (ACTIVE/INACTIVE)
- **createdAt**: Instant
- **updatedAt**: Instant

The entity follows DDD principles with:
- Private constructor, factory method `create()`
- Business logic methods: `updateDetails()`, `updateStock()`, `reserveStock()`, `releaseStock()`
- Domain invariants enforced in the entity

### 2. Redis Caching

**Location:** `infrastructure/config/CacheConfig.java`

Implemented Spring Cache with Redis:

```java
@Configuration
@EnableCaching
public class CacheConfig {
    public static final String PRODUCT_CACHE = "products";
    public static final String PRODUCT_LIST_CACHE = "productList";
}
```

**Cache Strategy:**
- **Product Cache**: 30-minute TTL for individual products
- **Product List Cache**: 5-minute TTL for lists
- JSON serialization using Jackson
- Null values not cached

**Caching Annotations in ProductService:**

| Method | Annotation | Purpose |
|--------|------------|---------|
| `getProduct(UUID id)` | `@Cacheable(value = PRODUCT_CACHE, key = "#id")` | Cache individual product lookups |
| `updateProduct(...)` | `@CacheEvict(value = PRODUCT_CACHE, key = "#id")` | Evict cache on product update |
| `updateStock(...)` | `@CacheEvict(value = PRODUCT_CACHE, key = "#id")` | Evict cache on stock update |
| `createProduct(...)` | `@CacheEvict(value = {PRODUCT_CACHE, PRODUCT_LIST_CACHE}, allEntries = true)` | Clear all caches on create |
| `deleteProduct(...)` | `@CacheEvict(value = {PRODUCT_CACHE, PRODUCT_LIST_CACHE}, allEntries = true)` | Clear all caches on delete |

### 3. Validation with JSR-303

**Location:** `application/dto/CreateProductRequest.java`, `UpdateProductRequest.java`

DTOs use standard Jakarta Validation annotations:

```java
public record CreateProductRequest(
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be less than 255 characters")
    String name,

    @NotBlank(message = "SKU is required")
    @Size(max = 50, message = "SKU must be less than 50 characters")
    String sku,

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    BigDecimal price,

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    Integer stockQuantity
) {}
```

Controllers use `@Valid` annotation to trigger validation.

### 4. Global Exception Handling

**Location:** `infrastructure/exception/GlobalExceptionHandler.java`

Standardized JSON error responses using `@RestControllerAdvice`:

**Exception Handlers:**

| Exception | Status Code | Response Format |
|-----------|-------------|-----------------|
| `ResourceNotFoundException` | 404 | `ErrorResponse` |
| `BusinessException` | 400 | `ErrorResponse` with error code |
| `MethodArgumentNotValidException` | 400 | `ValidationErrorResponse` with field errors |
| `IllegalArgumentException` | 400 | `ErrorResponse` |
| Generic `Exception` | 500 | `ErrorResponse` |

**Response Formats:**

```json
// ErrorResponse
{
  "status": 404,
  "error": "Resource Not Found",
  "message": "Product not found with id: 'xxx'",
  "timestamp": "2026-02-01T10:30:00Z"
}

// ValidationErrorResponse
{
  "status": 400,
  "error": "Validation Failed",
  "validationErrors": {
    "name": "Name is required",
    "price": "Price must be greater than 0"
  },
  "timestamp": "2026-02-01T10:30:00Z"
}
```

### 5. Testing with Testcontainers

**Location:** `src/test/java/com/ecommerce/product/`

#### ProductRepositoryTest (`@DataJpaTest`)

Tests JPA repository with PostgreSQL Testcontainer:
- `shouldSaveAndFindProduct()`
- `shouldFindProductById()`
- `shouldCheckIfProductExistsBySku()`
- `shouldFindAllProductsWithPagination()`
- `shouldFindProductsByCategory()`
- `shouldUpdateProduct()`
- `shouldDeleteProduct()`
- `shouldUpdateStockQuantity()`

#### ProductCacheIntegrationTest (`@SpringBootTest`)

Tests caching behavior with PostgreSQL and Redis Testcontainers:
- `shouldCacheProductOnFirstGet()` - Verifies products are cached
- `shouldEvictCacheOnUpdate()` - Verifies cache eviction on update
- `shouldEvictCacheOnStockUpdate()` - Verifies cache eviction on stock changes
- `shouldEvictAllCachesOnDelete()` - Verifies cache clearing on delete
- `shouldHandleMultipleProductsInCache()` - Verifies selective cache eviction

#### GlobalExceptionHandlerTest (`@SpringBootTest + @AutoConfigureMockMvc`)

Integration tests for exception handling:
- `shouldHandleResourceNotFoundException()` - 404 responses
- `shouldHandleValidationErrorsForMissingRequiredFields()` - Validation errors
- `shouldHandleValidationErrorsForInvalidValues()` - Constraint violations
- `shouldHandleBusinessExceptionForDuplicateSku()` - Business rule violations
- `shouldHandleIllegalArgumentException()` - Illegal argument handling
- `shouldReturnValidResponseForValidRequest()` - Happy path validation

## Configuration

### application.yml

```yaml
spring:
  application:
    name: product-service

  datasource:
    url: jdbc:postgresql://localhost:5434/product_db
    username: postgres
    password: postgres

  data:
    redis:
      host: localhost
      port: 6379
      timeout: 60000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0

  cache:
    type: redis
    redis:
      time-to-live: 600000
      cache-null-values: false

  kafka:
    bootstrap-servers: localhost:9092

server:
  port: 8082
```

### Dependencies (pom.xml)

Key dependencies added:
- `spring-boot-starter-data-redis` - Redis support
- `spring-boot-starter-validation` - JSR-303 validation
- `spring-boot-testcontainers` - Testcontainers integration
- `testcontainers:postgresql` - PostgreSQL Testcontainer
- `testcontainers:junit-jupiter` - JUnit 5 support

## Running the Service

### Start Infrastructure

```bash
docker-compose -f docker-compose-infra.yml up -d
```

This starts:
- PostgreSQL (port 5434)
- Redis (port 6379)
- Kafka + Zookeeper

### Build the Service

```bash
./mvnw clean install -pl product-service
```

### Run Tests

```bash
# Ensure Docker Desktop is running for Testcontainers
./mvnw test -pl product-service
```

### Run the Service

```bash
./mvnw spring-boot:run -pl product-service
```

Access:
- **API**: http://localhost:8082/api/products
- **Swagger UI**: http://localhost:8082/swagger-ui.html
- **API Docs**: http://localhost:8082/api-docs

## API Endpoints

### Create Product
```http
POST /api/products
Content-Type: application/json

{
  "name": "Laptop",
  "description": "High-performance laptop",
  "sku": "LAP-001",
  "price": 999.99,
  "stockQuantity": 50,
  "categoryId": "uuid-here"
}
```

### Get Product (Cached)
```http
GET /api/products/{id}
```

### Update Product (Cache Evicted)
```http
PUT /api/products/{id}
Content-Type: application/json

{
  "name": "Updated Laptop",
  "description": "Updated description",
  "price": 899.99
}
```

### Update Stock (Cache Evicted)
```http
PATCH /api/products/{id}/stock?quantity=75
```

### Delete Product (Cache Cleared)
```http
DELETE /api/products/{id}
```

### Get All Products (Paginated)
```http
GET /api/products?page=0&size=10&sort=name,asc
```

## Architecture

```
product-service/
├── domain/
│   ├── entity/
│   │   ├── Product.java           # Product aggregate root
│   │   └── Category.java
│   ├── valueobject/
│   │   ├── Money.java             # Price value object
│   │   └── ProductStatus.java
│   ├── repository/
│   │   └── ProductRepository.java # Repository interface
│   └── event/
│       └── ProductCreatedEvent.java
├── application/
│   ├── service/
│   │   └── ProductService.java    # Application service with caching
│   └── dto/
│       ├── ProductDto.java
│       ├── CreateProductRequest.java  # With validation
│       └── UpdateProductRequest.java  # With validation
├── infrastructure/
│   ├── config/
│   │   └── CacheConfig.java       # Redis cache configuration
│   ├── exception/
│   │   └── GlobalExceptionHandler.java  # Global exception handling
│   └── persistence/
│       ├── ProductJpaEntity.java
│       └── ProductJpaRepository.java
└── interfaces/
    └── rest/
        └── ProductController.java # REST API endpoints
```

## Testing Strategy

1. **Unit Tests**: Domain entity business logic
2. **Slice Tests**: `@DataJpaTest` for repository layer
3. **Integration Tests**: Full Spring context with Testcontainers
4. **Cache Tests**: Verify caching behavior with Redis
5. **Exception Tests**: Verify error responses and validation

## Production Considerations

✅ **Caching**: Redis-based caching with configurable TTL
✅ **Validation**: Comprehensive input validation
✅ **Error Handling**: Standardized error responses
✅ **Testing**: Integration tests with Testcontainers
✅ **Database**: PostgreSQL with JPA/Hibernate
✅ **Observability**: Actuator endpoints enabled
✅ **API Documentation**: OpenAPI/Swagger
✅ **Domain-Driven Design**: Clean architecture with DDD patterns

## Notes

- Tests require Docker Desktop running for Testcontainers
- Cache can be monitored via Redis CLI: `redis-cli -h localhost -p 6379`
- Database migrations managed via Flyway (see `src/main/resources/db/migration`)
- Events published to Kafka for downstream services
