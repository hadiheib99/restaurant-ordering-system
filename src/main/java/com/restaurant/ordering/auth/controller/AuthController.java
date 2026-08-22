package com.restaurant.ordering.auth.controller;

import com.restaurant.ordering.auth.dto.LoginRequest;
import com.restaurant.ordering.auth.dto.LoginResponse;
import com.restaurant.ordering.auth.dto.RegisterRequest;
import com.restaurant.ordering.auth.dto.VerifyRegistrationRequest;
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

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register/request")
    public Map<String, String> requestRegistration(@RequestBody RegisterRequest request) {
        authService.requestRegistration(request);
        return Map.of("message", "Verification code sent");
    }

    @PostMapping("/register/verify")
    public LoginResponse verifyRegistration(@RequestBody VerifyRegistrationRequest request) {
        return authService.verifyRegistration(request);
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return userService.getUserByEmail(authentication.getName());
    }
}
