package com.ecommerce.auth.domain.event;

import com.ecommerce.common.domain.DomainEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class UserRegisteredEvent extends DomainEvent {

    private final UUID userId;
    private final String email;
    private final String fullName;

    public UserRegisteredEvent(UUID userId, String email, String fullName) {
        super();
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
    }

    @Override
    public String getEventType() {
        return "USER_REGISTERED";
    }
}
