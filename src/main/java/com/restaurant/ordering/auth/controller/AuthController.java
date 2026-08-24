package com.restaurant.ordering.auth.controller;

import com.restaurant.ordering.auth.dto.LoginRequest;
import com.restaurant.ordering.auth.dto.LoginResponse;
import com.restaurant.ordering.auth.dto.RegisterRequest;
import com.restaurant.ordering.auth.service.AuthService;
import com.restaurant.ordering.dto.UserResponse;
import com.restaurant.ordering.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication and self-registration.
 *
 * <p>The controller exposes login, customer registration and current-user
 * endpoints. Successful authentication operations return a JWT that the
 * Angular client uses on protected REST requests.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    /**
     * Authenticates a user with email and password.
     * @param request login credentials supplied by the client
     * @return authentication response containing a signed JWT
     */
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /**
     * Registers a new customer account and immediately authenticates it.
     * @param request registration data supplied by the customer
     * @return authentication response containing a signed JWT
     */
    @PostMapping("/register")
    public LoginResponse register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    /**
     * Resolves the complete user profile associated with the current JWT.
     * @param authentication Spring Security authentication for the request
     * @return currently authenticated user
     */
    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return userService.getUserByEmail(authentication.getName());
    }
}
