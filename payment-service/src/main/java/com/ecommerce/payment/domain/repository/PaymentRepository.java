package com.ecommerce.payment.domain.repository;

import com.ecommerce.payment.domain.entity.Payment;
import com.ecommerce.payment.domain.valueobject.PaymentStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(UUID id);

    Optional<Payment> findByOrderId(UUID orderId);

    List<Payment> findByCustomerId(UUID customerId);

    List<Payment> findByStatus(PaymentStatus status);

    void delete(Payment payment);
}
