package com.ecommerce.payment.domain.entity;

import com.ecommerce.payment.domain.valueobject.OutboxStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {

    private UUID id;
    private UUID aggregateId;
    private String aggregateType;
    private String eventType;
    private String payload;
    private OutboxStatus status;
    private Integer retryCount;
    private String failReason;
    private Instant createdAt;
    private Instant processedAt;

    private OutboxEvent(UUID aggregateId, String aggregateType, String eventType, String payload) {
        this.id = UUID.randomUUID();
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.eventType = eventType;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.retryCount = 0;
        this.createdAt = Instant.now();
    }

    public static OutboxEvent create(UUID aggregateId, String aggregateType, String eventType, String payload) {
        return new OutboxEvent(aggregateId, aggregateType, eventType, payload);
    }

    public void markAsCompleted() {
        this.status = OutboxStatus.COMPLETED;
        this.processedAt = Instant.now();
    }

    public void markAsFailed(String reason) {
        this.retryCount++;
        this.failReason = reason;
        if (this.retryCount >= 3) {
            this.status = OutboxStatus.FAILED;
        }
    }
}
