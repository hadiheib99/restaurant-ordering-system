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

/**
 * Unit tests for {@link UserService} administrator user-management behavior.
 *
 * <p>The suite verifies password encoding, role persistence, duplicate-username
 * validation, password preservation during partial updates, enabled-state changes
 * and missing-user handling with mocked persistence and encoding collaborators.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    private UserService service;

    /** Creates a fresh user service with mocked collaborators before every test. */
    @BeforeEach
    void setUp() {
        service = new UserService(userRepository, passwordEncoder);
    }

    /** Verifies that creating a user encodes the supplied password and persists the requested role. */
    @Test
    void createUserEncodesPasswordAndPersistsRole() {
        UserRequest request = request("waiter1", "waiter@example.com", "Password1", Role.WAITER);
        when(passwordEncoder.encode("Password1")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.createUser(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("encoded", captor.getValue().getPassword());
        assertEquals(Role.WAITER, response.getRole());
    }

    /** Verifies that a duplicate username is rejected without storing another user. */
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

    /** Verifies that leaving the password blank during an update retains the existing encoded password. */
    @Test
    void updateUserKeepsPasswordWhenBlank() {
        User existing = user("chef", "chef@example.com", "stored", Role.CHEF);
        when(userRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(userRepository.findByUsernameIgnoreCase("chef-new")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("chef-new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(existing)).thenReturn(existing);

        UserRequest request = request("chef-new", "chef-new@example.com", "", Role.CHEF);
        service.updateUser(3L, request);

        assertEquals("stored", existing.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
    }

    /** Verifies that the administrator can change whether an existing user is enabled. */
    @Test
    void setEnabledUpdatesUser() {
        User existing = user("customer", "customer@example.com", "stored", Role.CUSTOMER);
        existing.setEnabled(true);
        when(userRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        var response = service.setEnabled(7L, false);

        assertFalse(response.isEnabled());
    }

    /** Verifies that requesting a nonexistent user raises the domain not-found exception. */
    @Test
    void missingUserThrowsResourceNotFound() {
        when(userRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getUserById(404L));
    }

    /**
     * Creates a request fixture shared by user-management tests.
     *
     * @param username account username
     * @param email account email
     * @param password raw password value
     * @param role requested restaurant role
     * @return initialized user request fixture
     */
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

    /**
     * Creates a user entity fixture with common display values.
     *
     * @param username account username
     * @param email account email
     * @param password stored encoded password placeholder
     * @param role restaurant role
     * @return initialized user fixture
     */
    private static User user(String username, String email, String password, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setFirstName("First");
        user.setLastName("Last");
        user.setRole(role);
        return user;
    }
}
