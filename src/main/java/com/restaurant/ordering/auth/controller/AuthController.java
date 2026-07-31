package com.restaurant.ordering.auth.controller;

import com.restaurant.ordering.auth.service.AuthService;
import com.restaurant.ordering.auth.dto.LoginRequest;
import com.restaurant.ordering.auth.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {

        return authService.login(request);
    }
}