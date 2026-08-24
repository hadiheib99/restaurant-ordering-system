package com.restaurant.ordering.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Strongly typed JWT configuration loaded from {@code jwt.*} application properties.
 *
 * @param issuer expected token issuer
 * @param secret HMAC secret used to sign and verify tokens
 * @param expirationMinutes token lifetime in minutes
 * @author Abdulhadi Heib
 * @version 1.0
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String issuer,
        String secret,
        long expirationMinutes
) {
}
