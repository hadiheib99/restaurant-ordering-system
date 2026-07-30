package com.restaurant.ordering.service;

import com.restaurant.ordering.dto.UserRequest;
import com.restaurant.ordering.dto.UserResponse;
import com.restaurant.ordering.model.Role;
import com.restaurant.ordering.model.User;
import com.restaurant.ordering.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponse getUserById(Long id) {
        return toResponse(findUser(id));
    }

    public List<UserResponse> getUsersByRole(Role role) {
        return userRepository.findByRole(role)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponse createUser(UserRequest request) {
        validateUniqueFields(request.getUsername(), request.getEmail(), null);

        User user = new User();
        updateUserFields(user, request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        return toResponse(userRepository.save(user));
    }

    public UserResponse updateUser(Long id, UserRequest request) {
        User user = findUser(id);

        validateUniqueFields(request.getUsername(), request.getEmail(), user);

        updateUserFields(user, request);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return toResponse(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        User user = findUser(id);
        userRepository.delete(user);
    }

    public UserResponse setEnabled(Long id, boolean enabled) {
        User user = findUser(id);
        user.setEnabled(enabled);

        return toResponse(userRepository.save(user));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found with id: " + id));
    }

    private void validateUniqueFields(
            String username,
            String email,
            User existingUser) {

        userRepository.findByUsernameIgnoreCase(username)
                .filter(user -> existingUser == null ||
                        !user.getId().equals(existingUser.getId()))
                .ifPresent(user -> {
                    throw new IllegalArgumentException(
                            "Username already exists"
                    );
                });

        userRepository.findByEmailIgnoreCase(email)
                .filter(user -> existingUser == null ||
                        !user.getId().equals(existingUser.getId()))
                .ifPresent(user -> {
                    throw new IllegalArgumentException(
                            "Email already exists"
                    );
                });
    }

    private void updateUserFields(User user, UserRequest request) {
        user.setUsername(request.getUsername());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }
}