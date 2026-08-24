package com.restaurant.ordering.dto;

import com.restaurant.ordering.model.Role;

import java.time.LocalDateTime;

/**
 * Password-free REST representation of a restaurant user account.
 *
 * <p>The DTO intentionally excludes the stored password hash and exposes only
 * profile, role, account-state and creation information needed by clients.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
public class UserResponse {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Role role;
    private boolean enabled;
    private LocalDateTime createdAt;

    /** Creates a complete response from a persistent user entity. */
    public UserResponse(Long id, String username, String firstName, String lastName,
                        String email, String phone, Role role, boolean enabled,
                        LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.enabled = enabled;
        this.createdAt = createdAt;
    }

    /** @return user identifier */
    public Long getId() { return id; }
    /** @return username */
    public String getUsername() { return username; }
    /** @return first name */
    public String getFirstName() { return firstName; }
    /** @return last name */
    public String getLastName() { return lastName; }
    /** @return email address */
    public String getEmail() { return email; }
    /** @return phone number */
    public String getPhone() { return phone; }
    /** @return application role */
    public Role getRole() { return role; }
    /** @return true when the account is enabled */
    public boolean isEnabled() { return enabled; }
    /** @return account creation timestamp */
    public LocalDateTime getCreatedAt() { return createdAt; }
}
