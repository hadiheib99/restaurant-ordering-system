package com.restaurant.ordering.auth.service;

import com.restaurant.ordering.auth.dto.LoginRequest;
import com.restaurant.ordering.auth.dto.RegisterRequest;
import com.restaurant.ordering.model.Role;
import com.restaurant.ordering.model.User;
import com.restaurant.ordering.repository.UserRepository;
import com.restaurant.ordering.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(authenticationManager, userRepository, jwtService, passwordEncoder);
    }

    @Test
    void loginAuthenticatesAndReturnsJwt() {
        LoginRequest request = new LoginRequest();
        request.setEmail("customer@restaurant.com");
        request.setPassword("Password123");

        User user = new User();
        user.setEmail(request.getEmail());
        user.setRole(Role.CUSTOMER);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(request.getEmail(), "CUSTOMER")).thenReturn("jwt-token");

        var response = authService.login(request);

        assertEquals("jwt-token", response.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void registerCreatesEnabledCustomerWithEncodedPassword() {
        RegisterRequest request = new RegisterRequest(
                " newcustomer ", "Password123", " Jane ", " Doe ",
                " JANE@EXAMPLE.COM ", " 0501234567 "
        );

        when(passwordEncoder.encode("Password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken("jane@example.com", "CUSTOMER")).thenReturn("new-token");

        var response = authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertEquals("newcustomer", saved.getUsername());
        assertEquals("encoded-password", saved.getPassword());
        assertEquals("Jane", saved.getFirstName());
        assertEquals("Doe", saved.getLastName());
        assertEquals("jane@example.com", saved.getEmail());
        assertEquals("0501234567", saved.getPhone());
        assertEquals(Role.CUSTOMER, saved.getRole());
        assertTrue(saved.isEnabled());
        assertEquals("new-token", response.getToken());
    }

    @Test
    void registerRejectsDuplicateEmail() {
        RegisterRequest request = new RegisterRequest(
                "customer2", "Password123", "Jane", "Doe",
                "jane@example.com", "0501234567"
        );
        when(userRepository.existsByEmailIgnoreCase("jane@example.com")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(request)
        );

        assertEquals("Email is already registered", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerRejectsWeakPassword() {
        RegisterRequest request = new RegisterRequest(
                "customer2", "short", "Jane", "Doe",
                "jane@example.com", "0501234567"
        );

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }
}
