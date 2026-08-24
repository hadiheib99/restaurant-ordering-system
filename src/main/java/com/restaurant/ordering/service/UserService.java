package com.restaurant.ordering.service;

import com.restaurant.ordering.dto.UserRequest;
import com.restaurant.ordering.dto.UserResponse;
import com.restaurant.ordering.exception.ResourceNotFoundException;
import com.restaurant.ordering.model.Role;
import com.restaurant.ordering.model.User;
import com.restaurant.ordering.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Business service for restaurant user accounts.
 *
 * <p>The service performs user CRUD operations, role filtering, password hashing,
 * uniqueness validation and account enable/disable operations. Password hashes
 * are never exposed through {@link UserResponse}.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates the user service.
     * @param userRepository persistence repository for users
     * @param passwordEncoder encoder used before storing passwords
     */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** @return all users converted to safe response DTOs */
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    /**
     * Retrieves a user by identifier.
     * @param id unique user identifier
     * @return matching user response
     * @throws ResourceNotFoundException when the user does not exist
     */
    public UserResponse getUserById(Long id) {
        return toResponse(findUser(id));
    }

    /**
     * Retrieves a user by email address.
     * @param email account email address
     * @return matching user response
     * @throws ResourceNotFoundException when no user has the email address
     */
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return toResponse(user);
    }

    /**
     * Finds users assigned to a given role.
     * @param role role used as a filter
     * @return users assigned to the role
     */
    public List<UserResponse> getUsersByRole(Role role) {
        return userRepository.findByRole(role).stream().map(this::toResponse).toList();
    }

    /**
     * Creates a user after validating uniqueness and password requirements.
     * @param request new account information
     * @return created user response
     * @throws IllegalArgumentException when username/email is already used or the password is invalid
     */
    public UserResponse createUser(UserRequest request) {
        validateUniqueFields(request.getUsername(), request.getEmail(), null);
        validateNewPassword(request.getPassword());

        User user = new User();
        updateUserFields(user, request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return toResponse(userRepository.save(user));
    }

    /**
     * Updates an existing user and hashes a newly supplied password when present.
     * @param id unique user identifier
     * @param request replacement user data
     * @return updated user response
     * @throws ResourceNotFoundException when the user does not exist
     * @throws IllegalArgumentException when uniqueness or password validation fails
     */
    public UserResponse updateUser(Long id, UserRequest request) {
        User user = findUser(id);
        validateUniqueFields(request.getUsername(), request.getEmail(), user);
        updateUserFields(user, request);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            validateNewPassword(request.getPassword());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return toResponse(userRepository.save(user));
    }

    /**
     * Deletes an existing user.
     * @param id unique user identifier
     * @throws ResourceNotFoundException when the user does not exist
     */
    public void deleteUser(Long id) {
        userRepository.delete(findUser(id));
    }

    /**
     * Changes whether an account may authenticate.
     * @param id unique user identifier
     * @param enabled new enabled state
     * @return updated user response
     */
    public UserResponse setEnabled(Long id, boolean enabled) {
        User user = findUser(id);
        user.setEnabled(enabled);
        return toResponse(userRepository.save(user));
    }

    /** @return persistent user with the requested id */
    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    /** Validates the minimum password requirements used for managed accounts. */
    private void validateNewPassword(String password) {
        if (password == null || password.isBlank() || password.length() < 6) {
            throw new IllegalArgumentException("Password must contain at least 6 characters");
        }
    }

    /** Ensures that username and email remain unique across user accounts. */
    private void validateUniqueFields(String username, String email, User existingUser) {
        userRepository.findByUsernameIgnoreCase(username)
                .filter(user -> existingUser == null || !user.getId().equals(existingUser.getId()))
                .ifPresent(user -> { throw new IllegalArgumentException("Username already exists"); });

        userRepository.findByEmailIgnoreCase(email)
                .filter(user -> existingUser == null || !user.getId().equals(existingUser.getId()))
                .ifPresent(user -> { throw new IllegalArgumentException("Email already exists"); });
    }

    /** Copies editable request fields to the persistent user entity. */
    private void updateUserFields(User user, UserRequest request) {
        user.setUsername(request.getUsername());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
    }

    /** Converts a user entity to the password-free REST response DTO. */
    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(), user.getUsername(), user.getFirstName(), user.getLastName(),
                user.getEmail(), user.getPhone(), user.getRole(), user.isEnabled(), user.getCreatedAt()
        );
    }
}
