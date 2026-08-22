package com.restaurant.ordering.service;

import com.restaurant.ordering.dto.UserRequest;
import com.restaurant.ordering.exception.ResourceNotFoundException;
import com.restaurant.ordering.model.Role;
import com.restaurant.ordering.model.User;
import com.restaurant.ordering.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void createUserEncodesPasswordAndPersistsRole() {
        UserRequest request = request("waiter1", "waiter@example.com", "Password1", Role.WAITER);
        when(passwordEncoder.encode("Password1")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.createUser(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("encoded", captor.getValue().getPassword());
        assertEquals(Role.WAITER, response.role());
    }

    @Test
    void createUserRejectsDuplicateUsername() {
        User existing = new User();
        existing.setUsername("waiter1");
        when(userRepository.findByUsernameIgnoreCase("waiter1")).thenReturn(Optional.of(existing));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.createUser(request("waiter1", "new@example.com", "Password1", Role.WAITER))
        );
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserKeepsPasswordWhenBlank() {
        User existing = user(3L, "chef", "chef@example.com", "stored", Role.CHEF);
        when(userRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(userRepository.findByUsernameIgnoreCase("chef-new")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("chef-new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(existing)).thenReturn(existing);

        UserRequest request = request("chef-new", "chef-new@example.com", "", Role.CHEF);
        service.updateUser(3L, request);

        assertEquals("stored", existing.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void setEnabledUpdatesUser() {
        User existing = user(7L, "customer", "customer@example.com", "stored", Role.CUSTOMER);
        existing.setEnabled(true);
        when(userRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        var response = service.setEnabled(7L, false);

        assertFalse(response.enabled());
    }

    @Test
    void missingUserThrowsResourceNotFound() {
        when(userRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getUserById(404L));
    }

    private static UserRequest request(String username, String email, String password, Role role) {
        UserRequest request = new UserRequest();
        request.setUsername(username);
        request.setEmail(email);
        request.setPassword(password);
        request.setFirstName("First");
        request.setLastName("Last");
        request.setPhone("0500000000");
        request.setRole(role);
        return request;
    }

    private static User user(Long id, String username, String email, String password, Role role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setFirstName("First");
        user.setLastName("Last");
        user.setRole(role);
        return user;
    }
}
