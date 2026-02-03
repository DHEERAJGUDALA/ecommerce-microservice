package com.ecommerce.order.infrastructure.messaging;

import com.ecommerce.order.domain.entity.OutboxEvent;
import com.ecommerce.order.domain.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPoller {

    private static final int BATCH_SIZE = 50;
    private static final int MAX_RETRIES = 3;
    private static final String TOPIC = "order-events";
    private static final long KAFKA_TIMEOUT_SECONDS = 30;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Polls for pending outbox events and publishes them to Kafka.
     * Runs every 5 seconds to ensure timely event delivery.
     */
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void pollAndPublish() {
        try {
            List<OutboxEvent> events = outboxEventRepository.findPendingEventsForUpdate(BATCH_SIZE);

            if (events.isEmpty()) {
                log.trace("No pending outbox events to process");
                return;
            }

            log.info("Processing batch of {} pending outbox events", events.size());

            int successCount = 0;
            int failureCount = 0;

            for (OutboxEvent event : events) {
                boolean success = processEvent(event);
                if (success) {
                    successCount++;
                } else {
                    failureCount++;
                }
            }

            log.info("Batch processing completed: {} succeeded, {} failed", successCount, failureCount);

        } catch (Exception e) {
            log.error("Unexpected error during outbox polling", e);
        }
    }

    /**
     * Processes a single outbox event by publishing it to Kafka.
     *
     * @param event The outbox event to process
     * @return true if successful, false otherwise
     */
    private boolean processEvent(OutboxEvent event) {
        log.debug("Processing outbox event: id={}, aggregateId={}, eventType={}",
            event.getId(), event.getAggregateId(), event.getEventType());

        try {
            // Send message to Kafka with order ID as key for partitioning
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(
                TOPIC,
                event.getAggregateId().toString(),
                event.getPayload()
            );

            // Wait for acknowledgment from Kafka (blocking for exactly-once semantics)
            SendResult<String, String> result = future.get(KAFKA_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            // Mark event as completed
            event.markAsCompleted();
            outboxEventRepository.save(event);

            log.info("Successfully published event: id={}, eventType={}, partition={}, offset={}",
                event.getId(),
                event.getEventType(),
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset());

            return true;

        } catch (Exception e) {
            handleFailure(event, e);
            return false;
        }
    }

    /**
     * Handles event processing failures with retry logic.
     *
     * @param event The failed event
     * @param e The exception that caused the failure
     */
    private void handleFailure(OutboxEvent event, Exception e) {
        String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();

        log.error("Failed to publish event: id={}, aggregateId={}, eventType={}, error={}",
            event.getId(),
            event.getAggregateId(),
            event.getEventType(),
            errorMessage,
            e);

        // Update event with failure information
        event.markAsFailed(errorMessage);
        outboxEventRepository.save(event);

        if (event.getRetryCount() >= MAX_RETRIES) {
            log.error("Event PERMANENTLY FAILED after {} retries: id={}, aggregateId={}, eventType={}, reason={}",
                MAX_RETRIES,
                event.getId(),
                event.getAggregateId(),
                event.getEventType(),
                errorMessage);

            // TODO: Consider sending alert to monitoring system (e.g., PagerDuty, Slack)
            // TODO: Consider moving to a dead-letter table for manual review

        } else {
            log.warn("Event will be retried: id={}, retryCount={}/{}, nextRetryIn=5s",
                event.getId(),
                event.getRetryCount(),
                MAX_RETRIES);
        }
    }
}
