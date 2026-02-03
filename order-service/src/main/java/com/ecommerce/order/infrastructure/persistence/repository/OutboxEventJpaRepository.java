package com.ecommerce.order.infrastructure.persistence.repository;

import com.ecommerce.order.infrastructure.persistence.entity.OutboxEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {

    @Query(nativeQuery = true, value = """
        SELECT * FROM outbox_events
        WHERE status = 'PENDING' AND retry_count < 3
        ORDER BY created_at ASC
        LIMIT :batchSize
        FOR UPDATE SKIP LOCKED
        """)
    List<OutboxEventJpaEntity> findPendingEventsWithSkipLocked(@Param("batchSize") int batchSize);
}
