package com.restaurant.ordering.auth.service;

import com.restaurant.ordering.auth.dto.LoginRequest;
import com.restaurant.ordering.auth.dto.LoginResponse;
import com.restaurant.ordering.auth.dto.RegisterRequest;
import com.restaurant.ordering.model.Role;
import com.restaurant.ordering.model.User;
import com.restaurant.ordering.repository.UserRepository;
import com.restaurant.ordering.security.jwt.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        return new LoginResponse(generateToken(user));
    }

    public LoginResponse register(RegisterRequest request) {
        validateRegistration(request);

        User user = new User();
        user.setUsername(request.username().trim());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(request.email().trim().toLowerCase(Locale.ROOT));
        user.setPhone(request.phone().trim());
        user.setRole(Role.CUSTOMER);
        user.setEnabled(true);

        User saved = userRepository.save(user);
        return new LoginResponse(generateToken(saved));
    }

    private void validateRegistration(RegisterRequest request) {
        if (request == null || isBlank(request.username()) || isBlank(request.password()) ||
                isBlank(request.firstName()) || isBlank(request.lastName()) ||
                isBlank(request.email()) || isBlank(request.phone())) {
            throw new IllegalArgumentException("All registration fields are required");
        }

        if (request.password().length() < 8) {
            throw new IllegalArgumentException("Password must contain at least 8 characters");
        }

        String email = request.email().trim();
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Invalid email address");
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }
        if (userRepository.existsByUsernameIgnoreCase(request.username().trim())) {
            throw new IllegalArgumentException("Username is already registered");
        }
    }

    private String generateToken(User user) {
        return jwtService.generateToken(user.getEmail(), user.getRole().name());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
