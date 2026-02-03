package com.ecommerce.payment.infrastructure.persistence.adapter;

import com.ecommerce.payment.domain.entity.OutboxEvent;
import com.ecommerce.payment.domain.repository.OutboxEventRepository;
import com.ecommerce.payment.infrastructure.persistence.entity.OutboxEventJpaEntity;
import com.ecommerce.payment.infrastructure.persistence.mapper.OutboxEventPersistenceMapper;
import com.ecommerce.payment.infrastructure.persistence.repository.OutboxEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OutboxEventRepositoryAdapter implements OutboxEventRepository {

    private final OutboxEventJpaRepository jpaRepository;
    private final OutboxEventPersistenceMapper mapper;

    @Override
    public OutboxEvent save(OutboxEvent event) {
        OutboxEventJpaEntity entity = mapper.toJpaEntity(event);
        jpaRepository.save(entity);

        // Return the domain object with updated state
        updateDomainFromEntity(event, entity);
        return event;
    }

    @Override
    public List<OutboxEvent> findPendingEventsForUpdate(int batchSize) {
        List<OutboxEventJpaEntity> entities = jpaRepository.findPendingEventsWithSkipLocked(batchSize);

        // Convert to domain events
        return entities.stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    private OutboxEvent toDomain(OutboxEventJpaEntity entity) {
        // Create domain event using static factory method
        OutboxEvent event = OutboxEvent.create(
            entity.getAggregateId(),
            entity.getAggregateType(),
            entity.getEventType(),
            entity.getPayload()
        );

        // Update internal state using setters
        updateDomainFromEntity(event, entity);
        return event;
    }

    private void updateDomainFromEntity(OutboxEvent event, OutboxEventJpaEntity entity) {
        event.setId(entity.getId());
        event.setStatus(entity.getStatus());
        event.setRetryCount(entity.getRetryCount());
        event.setFailReason(entity.getFailReason());
        event.setCreatedAt(entity.getCreatedAt());
        event.setProcessedAt(entity.getProcessedAt());
    }
}
