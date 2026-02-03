package com.ecommerce.order.infrastructure.persistence.adapter;

import com.ecommerce.order.domain.entity.Order;
import com.ecommerce.order.domain.repository.OrderRepository;
import com.ecommerce.order.domain.valueobject.OrderStatus;
import com.ecommerce.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.ecommerce.order.infrastructure.persistence.mapper.OrderPersistenceMapper;
import com.ecommerce.order.infrastructure.persistence.repository.OrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;
    private final OrderPersistenceMapper mapper;

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = mapper.toJpaEntity(order);

        // Set bidirectional relationship for order items
        entity.getItems().forEach(item -> item.setOrder(entity));

        jpaRepository.save(entity);
        return order;
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Page<Order> findByCustomerId(UUID customerId, Pageable pageable) {
        throw new UnsupportedOperationException("Find by customer ID not yet implemented - requires domain reconstruction");
    }

    @Override
    public Page<Order> findByStatus(OrderStatus status, Pageable pageable) {
        throw new UnsupportedOperationException("Find by status not yet implemented - requires domain reconstruction");
    }

    @Override
    public void delete(Order order) {
        jpaRepository.deleteById(order.getId());
    }
}

