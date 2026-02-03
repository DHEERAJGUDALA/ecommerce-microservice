package com.ecommerce.payment.infrastructure.persistence.adapter;

import com.ecommerce.payment.domain.entity.Payment;
import com.ecommerce.payment.domain.repository.PaymentRepository;
import com.ecommerce.payment.domain.valueobject.PaymentStatus;
import com.ecommerce.payment.infrastructure.persistence.entity.PaymentJpaEntity;
import com.ecommerce.payment.infrastructure.persistence.mapper.PaymentPersistenceMapper;
import com.ecommerce.payment.infrastructure.persistence.repository.PaymentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final PaymentJpaRepository jpaRepository;
    private final PaymentPersistenceMapper mapper;

    @Override
    public Payment save(Payment payment) {
        PaymentJpaEntity entity = mapper.toJpaEntity(payment);
        jpaRepository.save(entity);
        return payment;
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Optional<Payment> findByOrderId(UUID orderId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<Payment> findByCustomerId(UUID customerId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<Payment> findByStatus(PaymentStatus status) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void delete(Payment payment) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
