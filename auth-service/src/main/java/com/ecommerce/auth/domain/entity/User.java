package com.ecommerce.auth.domain.entity;

import com.ecommerce.auth.domain.valueobject.Email;
import com.ecommerce.auth.domain.valueobject.Role;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    private UUID id;
    private Email email;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private Set<Role> roles;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    private User(UUID id, Email email, String passwordHash, String firstName, String lastName) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.roles = new HashSet<>();
        this.roles.add(Role.USER);
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static User create(Email email, String passwordHash, String firstName, String lastName) {
        return new User(UUID.randomUUID(), email, passwordHash, firstName, lastName);
    }

    public void addRole(Role role) {
        this.roles.add(role);
        this.updatedAt = Instant.now();
    }

    public void removeRole(Role role) {
        if (role != Role.USER) {
            this.roles.remove(role);
            this.updatedAt = Instant.now();
        }
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.active = true;
        this.updatedAt = Instant.now();
    }

    public void updatePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
        this.updatedAt = Instant.now();
    }

    public void updateProfile(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.updatedAt = Instant.now();
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }
}
