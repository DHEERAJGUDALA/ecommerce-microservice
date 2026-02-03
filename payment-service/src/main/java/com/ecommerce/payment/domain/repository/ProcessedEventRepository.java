package com.ecommerce.payment.domain.repository;

import com.ecommerce.payment.domain.entity.ProcessedEvent;

import java.util.UUID;

public interface ProcessedEventRepository {

    ProcessedEvent save(ProcessedEvent event);

    boolean existsById(UUID eventId);
}
