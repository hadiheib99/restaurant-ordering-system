package com.restaurant.ordering.auth.dto;

/**
 * Immutable request DTO used for public customer registration.
 *
 * <p>The backend validates these values and always assigns the CUSTOMER role;
 * privileged roles cannot be selected through this public request.</p>
 *
 * @param username requested unique username
 * @param password plain-text password to validate and encode
 * @param firstName customer's first name
 * @param lastName customer's last name
 * @param email requested unique email address
 * @param phone customer contact phone number
 * @author Abdulhadi Heib
 * @version 1.0
 */
public record RegisterRequest(
        String username,
        String password,
        String firstName,
        String lastName,
        String email,
        String phone
) {
}
