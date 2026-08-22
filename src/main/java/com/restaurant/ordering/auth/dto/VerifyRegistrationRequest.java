package com.restaurant.ordering.auth.dto;

public record VerifyRegistrationRequest(
        String email,
        String code
) {
}
