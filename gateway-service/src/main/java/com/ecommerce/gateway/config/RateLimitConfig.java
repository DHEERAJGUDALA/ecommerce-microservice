package com.ecommerce.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Rate Limiting Configuration for API Gateway
 *
 * Uses Redis to track request rates per user/IP address.
 *
 * Rate limits configured in application.yml per route:
 * - replenishRate: Number of requests per second
 * - burstCapacity: Maximum requests allowed in a burst
 *
 * Examples:
 * - Auth endpoints: 10 req/s, burst 20
 * - Order endpoints: 30 req/s, burst 60
 * - Product endpoints: 50 req/s, burst 100
 */
@Configuration
@Slf4j
public class RateLimitConfig {

    /**
     * Key resolver for rate limiting
     *
     * Strategy:
     * 1. For authenticated requests: Use user ID from X-User-Id header
     * 2. For unauthenticated requests: Use client IP address
     *
     * This prevents:
     * - Brute force attacks on authentication endpoints
     * - API abuse from individual users
     * - DDoS attacks from single IP addresses
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            // Try to get user ID from header (set by AuthenticationFilter)
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");

            if (userId != null && !userId.isEmpty()) {
                log.trace("Rate limiting by user ID: {}", userId);
                return Mono.just("user:" + userId);
            }

            // Fall back to IP address for unauthenticated requests
            String ipAddress = Objects.requireNonNull(
                exchange.getRequest().getRemoteAddress()
            ).getAddress().getHostAddress();

            log.trace("Rate limiting by IP address: {}", ipAddress);
            return Mono.just("ip:" + ipAddress);
        };
    }
}
