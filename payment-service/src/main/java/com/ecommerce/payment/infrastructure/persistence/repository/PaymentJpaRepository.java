package com.ecommerce.payment.infrastructure.persistence.repository;

import com.ecommerce.payment.domain.valueobject.PaymentStatus;
import com.ecommerce.payment.infrastructure.persistence.entity.PaymentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, UUID> {

    Optional<PaymentJpaEntity> findByOrderId(UUID orderId);

    List<PaymentJpaEntity> findByCustomerId(UUID customerId);

    List<PaymentJpaEntity> findByStatus(PaymentStatus status);
}
