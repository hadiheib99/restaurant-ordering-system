package com.restaurant.ordering.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Authentication response returned after successful login or registration.
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@Data
@AllArgsConstructor
public class LoginResponse {
    /** Signed JWT used as a Bearer token on protected API requests. */
    private String token;
}
