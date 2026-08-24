package com.restaurant.ordering.auth.dto;

import lombok.Data;

/**
 * Authentication request containing the credentials submitted by a user.
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@Data
public class LoginRequest {
    /** Email address used as the Spring Security principal. */
    private String email;
    /** Plain-text password submitted only for authentication verification. */
    private String password;
}
