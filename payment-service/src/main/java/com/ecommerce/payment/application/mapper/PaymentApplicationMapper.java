package com.ecommerce.payment.application.mapper;

import com.ecommerce.payment.application.dto.PaymentEventPayload;
import com.ecommerce.payment.domain.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentApplicationMapper {

    @Mapping(target = "eventType", ignore = true)
    PaymentEventPayload toPaymentEventPayload(Payment payment);
}
