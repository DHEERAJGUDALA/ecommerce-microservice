package com.ecommerce.common.domain;

import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

/**
 * Base class for all domain entities.
 * Provides identity-based equality.
 */
@Getter
public abstract class BaseEntity<ID extends Serializable> {

    protected ID id;

    protected BaseEntity() {}

    protected BaseEntity(ID id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity<?> that = (BaseEntity<?>) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
