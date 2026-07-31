package com.restaurant.ordering.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String issuer,
        String secret,
        long expirationMinutes
) {
}