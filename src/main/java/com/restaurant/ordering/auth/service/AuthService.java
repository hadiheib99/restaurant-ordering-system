package com.restaurant.ordering.auth.service;

import com.restaurant.ordering.auth.dto.LoginRequest;
import com.restaurant.ordering.auth.dto.LoginResponse;
import com.restaurant.ordering.model.User;
import com.restaurant.ordering.repository.UserRepository;
import com.restaurant.ordering.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        return new LoginResponse(token);
    }
}