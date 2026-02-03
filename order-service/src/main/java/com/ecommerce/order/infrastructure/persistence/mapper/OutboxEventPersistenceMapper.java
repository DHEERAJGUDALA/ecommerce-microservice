package com.ecommerce.order.infrastructure.persistence.mapper;

import com.ecommerce.order.domain.entity.OutboxEvent;
import com.ecommerce.order.infrastructure.persistence.entity.OutboxEventJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventPersistenceMapper {

    public OutboxEventJpaEntity toJpaEntity(OutboxEvent event) {
        if (event == null) {
            return null;
        }

        return OutboxEventJpaEntity.builder()
            .id(event.getId())
            .aggregateId(event.getAggregateId())
            .aggregateType(event.getAggregateType())
            .eventType(event.getEventType())
            .payload(event.getPayload())
            .status(event.getStatus())
            .retryCount(event.getRetryCount())
            .failReason(event.getFailReason())
            .createdAt(event.getCreatedAt())
            .processedAt(event.getProcessedAt())
            .build();
    }

    public OutboxEvent toDomain(OutboxEventJpaEntity entity) {
        // Cannot reconstruct domain entity with protected constructor
        throw new UnsupportedOperationException("Conversion from JPA to domain not supported");
    }
}

