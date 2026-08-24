package com.restaurant.ordering.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing an authenticated restaurant-system user.
 *
 * <p>Accounts have a unique username/email, BCrypt password hash, contact data,
 * application {@link Role}, enabled state and creation timestamp. The same model
 * supports customers and restaurant staff.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_user_email", columnNames = "email")
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    /** Encoded password; plain-text passwords are never stored. */
    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, length = 120)
    private String email;

    @Column(length = 30)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Creates an empty entity required by JPA. */
    public User() { }

    /** Sets the account creation timestamp immediately before insertion. */
    @PrePersist
    public void prePersist() { createdAt = LocalDateTime.now(); }

    /** @return database-generated user identifier */
    public Long getId() { return id; }

    /** @return unique login/display username */
    public String getUsername() { return username; }

    /** @param username new unique username */
    public void setUsername(String username) { this.username = username; }

    /** @return stored encoded password */
    public String getPassword() { return password; }

    /** @param password encoded password to persist */
    public void setPassword(String password) { this.password = password; }

    /** @return user's first name */
    public String getFirstName() { return firstName; }

    /** @param firstName user's first name */
    public void setFirstName(String firstName) { this.firstName = firstName; }

    /** @return user's last name */
    public String getLastName() { return lastName; }

    /** @param lastName user's last name */
    public void setLastName(String lastName) { this.lastName = lastName; }

    /** @return unique account email address */
    public String getEmail() { return email; }

    /** @param email unique account email address */
    public void setEmail(String email) { this.email = email; }

    /** @return contact phone number */
    public String getPhone() { return phone; }

    /** @param phone contact phone number */
    public void setPhone(String phone) { this.phone = phone; }

    /** @return authorization role assigned to the account */
    public Role getRole() { return role; }

    /** @param role authorization role to assign */
    public void setRole(Role role) { this.role = role; }

    /** @return true when the account may authenticate */
    public boolean isEnabled() { return enabled; }

    /** @param enabled whether authentication is permitted */
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    /** @return timestamp at which the account was created */
    public LocalDateTime getCreatedAt() { return createdAt; }
}
