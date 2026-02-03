package com.ecommerce.order.infrastructure.persistence.mapper;

import com.ecommerce.order.domain.entity.Order;
import com.ecommerce.order.domain.entity.OrderItem;
import com.ecommerce.order.domain.valueobject.Address;
import com.ecommerce.order.infrastructure.persistence.entity.AddressEmbeddable;
import com.ecommerce.order.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.ecommerce.order.infrastructure.persistence.entity.OrderJpaEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class OrderPersistenceMapper {

    public OrderJpaEntity toJpaEntity(Order order) {
        if (order == null) {
            return null;
        }

        return OrderJpaEntity.builder()
            .id(order.getId())
            .customerId(order.getCustomerId())
            .shippingAddress(toEmbeddable(order.getShippingAddress()))
            .billingAddress(toEmbeddable(order.getBillingAddress()))
            .status(order.getStatus())
            .subtotal(order.getSubtotal())
            .tax(order.getTax())
            .shippingCost(order.getShippingCost())
            .total(order.getTotal())
            .currency(order.getCurrency())
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .items(order.getItems().stream()
                .map(this::toJpaEntity)
                .collect(Collectors.toList()))
            .build();
    }

    public Order toDomain(OrderJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        try {
            // Use reflection to reconstruct the domain object
            java.lang.reflect.Constructor<Order> constructor = Order.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Order order = constructor.newInstance();

            // Set fields using reflection
            setField(order, "id", entity.getId());
            setField(order, "customerId", entity.getCustomerId());
            setField(order, "shippingAddress", toDomain(entity.getShippingAddress()));
            setField(order, "billingAddress", toDomain(entity.getBillingAddress()));
            setField(order, "status", entity.getStatus());
            setField(order, "subtotal", entity.getSubtotal());
            setField(order, "tax", entity.getTax());
            setField(order, "shippingCost", entity.getShippingCost());
            setField(order, "total", entity.getTotal());
            setField(order, "currency", entity.getCurrency());
            setField(order, "createdAt", entity.getCreatedAt());
            setField(order, "updatedAt", entity.getUpdatedAt());

            // Reconstruct items
            java.util.List<OrderItem> items = entity.getItems().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
            setField(order, "items", items);

            return order;
        } catch (Exception e) {
            throw new RuntimeException("Failed to reconstruct Order domain entity", e);
        }
    }

    public OrderItemJpaEntity toJpaEntity(OrderItem item) {
        if (item == null) {
            return null;
        }

        return OrderItemJpaEntity.builder()
            .id(item.getId())
            .productId(item.getProductId())
            .productName(item.getProductName())
            .productSku(item.getProductSku())
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .totalPrice(item.getTotalPrice())
            .build();
    }

    public OrderItem toDomain(OrderItemJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        try {
            // Use reflection to reconstruct the domain object
            java.lang.reflect.Constructor<OrderItem> constructor = OrderItem.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            OrderItem item = constructor.newInstance();

            // Set fields using reflection
            setField(item, "id", entity.getId());
            setField(item, "productId", entity.getProductId());
            setField(item, "productName", entity.getProductName());
            setField(item, "productSku", entity.getProductSku());
            setField(item, "quantity", entity.getQuantity());
            setField(item, "unitPrice", entity.getUnitPrice());
            setField(item, "totalPrice", entity.getTotalPrice());

            return item;
        } catch (Exception e) {
            throw new RuntimeException("Failed to reconstruct OrderItem domain entity", e);
        }
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }

    public AddressEmbeddable toEmbeddable(Address address) {
        if (address == null) {
            return null;
        }

        return AddressEmbeddable.builder()
            .street(address.street())
            .city(address.city())
            .state(address.state())
            .zipCode(address.postalCode())
            .country(address.country())
            .build();
    }

    public Address toDomain(AddressEmbeddable embeddable) {
        if (embeddable == null) {
            return null;
        }

        return new Address(
            embeddable.getStreet(),
            embeddable.getCity(),
            embeddable.getState(),
            embeddable.getZipCode(),
            embeddable.getCountry()
        );
    }
}

