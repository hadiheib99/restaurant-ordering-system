package com.restaurant.ordering.controller;

import com.restaurant.ordering.dto.UserRequest;
import com.restaurant.ordering.dto.UserResponse;
import com.restaurant.ordering.model.Role;
import com.restaurant.ordering.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for administrative user management.
 *
 * <p>Provides endpoints to list users, retrieve a user, filter users by role,
 * create/update accounts, enable or disable accounts and delete users. Security
 * rules determine which authenticated roles may access each operation.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    /**
     * Creates the controller with its required user service.
     * @param userService service that implements user business logic
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** @return all users visible to the authenticated administrator */
    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    /**
     * Retrieves one user by identifier.
     * @param id unique user identifier
     * @return requested user data
     */
    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    /**
     * Retrieves users that have a specific application role.
     * @param role role used to filter users
     * @return users assigned to the requested role
     */
    @GetMapping("/role/{role}")
    public List<UserResponse> getUsersByRole(@PathVariable Role role) {
        return userService.getUsersByRole(role);
    }

    /**
     * Creates a new restaurant user account.
     * @param request validated user information
     * @return HTTP 201 response containing the created user
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        UserResponse createdUser = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    /**
     * Updates an existing user account.
     * @param id unique user identifier
     * @param request validated replacement user information
     * @return updated user data
     */
    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return userService.updateUser(id, request);
    }

    /**
     * Enables or disables an existing account.
     * @param id unique user identifier
     * @param value new enabled state
     * @return updated user data
     */
    @PatchMapping("/{id}/enabled")
    public UserResponse setEnabled(@PathVariable Long id, @RequestParam boolean value) {
        return userService.setEnabled(id, value);
    }

    /**
     * Deletes a user account.
     * @param id unique user identifier
     * @return HTTP 204 response after successful deletion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
