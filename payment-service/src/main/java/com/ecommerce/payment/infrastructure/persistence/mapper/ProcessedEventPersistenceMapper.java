package com.ecommerce.payment.infrastructure.persistence.mapper;

import com.ecommerce.payment.domain.entity.ProcessedEvent;
import com.ecommerce.payment.infrastructure.persistence.entity.ProcessedEventJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ProcessedEventPersistenceMapper {

    public ProcessedEventJpaEntity toJpaEntity(ProcessedEvent event) {
        if (event == null) {
            return null;
        }

        return ProcessedEventJpaEntity.builder()
            .eventId(event.getEventId())
            .eventType(event.getEventType())
            .processedAt(event.getProcessedAt())
            .build();
    }

    public ProcessedEvent toDomain(ProcessedEventJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new ProcessedEvent(
            entity.getEventId(),
            entity.getEventType(),
            entity.getProcessedAt()
        );
    }
}
