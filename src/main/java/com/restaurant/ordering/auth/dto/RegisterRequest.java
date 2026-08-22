package com.restaurant.ordering.auth.dto;

public record RegisterRequest(
        String username,
        String password,
        String firstName,
        String lastName,
        String email,
        String phone
) {
}
