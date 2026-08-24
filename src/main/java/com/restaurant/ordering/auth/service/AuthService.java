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

/**
 * Authentication service for login and customer self-registration.
 *
 * <p>Credentials are verified by Spring Security, passwords are hashed before
 * persistence, and successful authentication produces a JWT through
 * {@link JwtService}. Public registration always creates a {@link Role#CUSTOMER}
 * account so users cannot self-register as privileged staff.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates the authentication service with required security dependencies.
     * @param authenticationManager Spring Security authentication manager
     * @param userRepository user persistence repository
     * @param jwtService JWT generation service
     * @param passwordEncoder password hashing component
     */
    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Authenticates an existing account and returns a signed JWT.
     * @param request email and password credentials
     * @return JWT login response
     * @throws org.springframework.security.core.AuthenticationException when credentials are invalid
     */
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        return new LoginResponse(generateToken(user));
    }

    /**
     * Registers a new customer account after validating all input fields.
     * @param request customer registration data
     * @return JWT response for the newly created account
     * @throws IllegalArgumentException when required data is invalid or already registered
     */
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

    /** Validates presence, password length, email format and unique account fields. */
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

    /** Generates a JWT containing the user's email and application role. */
    private String generateToken(User user) {
        return jwtService.generateToken(user.getEmail(), user.getRole().name());
    }

    /** @return true when the supplied text is null, empty or whitespace only */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
