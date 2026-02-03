package com.ecommerce.order.application.mapper;

import com.ecommerce.order.application.dto.*;
import com.ecommerce.order.domain.entity.Order;
import com.ecommerce.order.domain.entity.OrderItem;
import com.ecommerce.order.domain.valueobject.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface OrderApplicationMapper {

    @Mapping(target = "status", expression = "java(order.getStatus().name())")
    OrderDto toDto(Order order);

    OrderItemDto toDto(OrderItem item);

    AddressDto toDto(Address address);

    Address toDomain(AddressDto dto);

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "status", expression = "java(order.getStatus().name())")
    @Mapping(target = "timestamp", expression = "java(java.time.Instant.now())")
    OrderEventPayload toEventPayload(Order order);
}
