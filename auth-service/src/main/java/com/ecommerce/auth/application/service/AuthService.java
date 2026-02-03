package com.ecommerce.auth.application.service;

import com.ecommerce.auth.application.dto.*;
import com.ecommerce.auth.domain.entity.User;
import com.ecommerce.auth.domain.event.UserRegisteredEvent;
import com.ecommerce.auth.domain.repository.UserRepository;
import com.ecommerce.auth.domain.service.PasswordService;
import com.ecommerce.auth.domain.valueobject.Email;
import com.ecommerce.auth.infrastructure.security.JwtTokenProvider;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ApplicationEventPublisher eventPublisher;

    public AuthService(UserRepository userRepository,
                       PasswordService passwordService,
                       JwtTokenProvider jwtTokenProvider,
                       ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.eventPublisher = eventPublisher;
    }

    public AuthResponse register(RegisterRequest request) {
        Email email = new Email(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("USER_EXISTS", "User with this email already exists");
        }

        String passwordHash = passwordService.encode(request.password());
        User user = User.create(email, passwordHash, request.firstName(), request.lastName());

        user = userRepository.save(user);

        eventPublisher.publishEvent(new UserRegisteredEvent(
                user.getId(),
                user.getEmail().value(),
                user.getFullName()
        ));

        return generateAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        Email email = new Email(request.email());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("INVALID_CREDENTIALS", "Invalid email or password"));

        if (!user.isActive()) {
            throw new BusinessException("USER_INACTIVE", "User account is deactivated");
        }

        if (!passwordService.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException("INVALID_CREDENTIALS", "Invalid email or password");
        }

        return generateAuthResponse(user);
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException("INVALID_TOKEN", "Invalid or expired refresh token");
        }

        String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(java.util.UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return generateAuthResponse(user);
    }

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        UserDto userDto = new UserDto(
                user.getId(),
                user.getEmail().value(),
                user.getFirstName(),
                user.getLastName(),
                user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()),
                user.isActive()
        );

        return AuthResponse.of(accessToken, refreshToken, jwtTokenProvider.getAccessTokenExpiration(), userDto);
    }
}
