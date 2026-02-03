package com.ecommerce.payment.infrastructure.persistence.adapter;

import com.ecommerce.payment.domain.entity.ProcessedEvent;
import com.ecommerce.payment.domain.repository.ProcessedEventRepository;
import com.ecommerce.payment.infrastructure.persistence.entity.ProcessedEventJpaEntity;
import com.ecommerce.payment.infrastructure.persistence.mapper.ProcessedEventPersistenceMapper;
import com.ecommerce.payment.infrastructure.persistence.repository.ProcessedEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProcessedEventRepositoryAdapter implements ProcessedEventRepository {

    private final ProcessedEventJpaRepository jpaRepository;
    private final ProcessedEventPersistenceMapper mapper;

    @Override
    public ProcessedEvent save(ProcessedEvent event) {
        ProcessedEventJpaEntity entity = mapper.toJpaEntity(event);
        jpaRepository.save(entity);
        return event;
    }

    @Override
    public boolean existsById(UUID eventId) {
        return jpaRepository.existsById(eventId);
    }
}
