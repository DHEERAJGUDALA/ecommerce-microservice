package com.ecommerce.order.domain.repository;

import com.ecommerce.order.domain.entity.OutboxEvent;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository {

    OutboxEvent save(OutboxEvent event);

    List<OutboxEvent> findPendingEventsForUpdate(int batchSize);
}
