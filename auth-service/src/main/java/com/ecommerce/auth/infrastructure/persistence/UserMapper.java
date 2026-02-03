package com.ecommerce.auth.infrastructure.persistence;

import com.ecommerce.auth.domain.entity.User;
import com.ecommerce.auth.domain.valueobject.Email;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Component
public class UserMapper {

    public UserJpaEntity toJpaEntity(User user) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.getId());
        entity.setEmail(user.getEmail().value());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setRoles(user.getRoles());
        entity.setActive(user.isActive());
        return entity;
    }

    public User toDomain(UserJpaEntity entity) {
        try {
            User user = createUserInstance();
            setField(user, "id", entity.getId());
            setField(user, "email", new Email(entity.getEmail()));
            setField(user, "passwordHash", entity.getPasswordHash());
            setField(user, "firstName", entity.getFirstName());
            setField(user, "lastName", entity.getLastName());
            setField(user, "roles", entity.getRoles());
            setField(user, "active", entity.isActive());
            setField(user, "createdAt", entity.getCreatedAt());
            setField(user, "updatedAt", entity.getUpdatedAt());
            return user;
        } catch (Exception e) {
            throw new RuntimeException("Failed to map JPA entity to domain", e);
        }
    }

    private User createUserInstance() throws Exception {
        var constructor = User.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private void setField(User user, String fieldName, Object value) throws Exception {
        Field field = User.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(user, value);
    }
}
