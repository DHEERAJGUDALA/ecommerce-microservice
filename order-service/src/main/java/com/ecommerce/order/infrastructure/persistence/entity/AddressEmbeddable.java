package com.ecommerce.order.infrastructure.persistence.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressEmbeddable {
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
