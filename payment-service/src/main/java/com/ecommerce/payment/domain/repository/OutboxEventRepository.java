package com.ecommerce.payment.domain.repository;

import com.ecommerce.payment.domain.entity.OutboxEvent;

import java.util.List;

public interface OutboxEventRepository {

    OutboxEvent save(OutboxEvent event);

    List<OutboxEvent> findPendingEventsForUpdate(int batchSize);
}
