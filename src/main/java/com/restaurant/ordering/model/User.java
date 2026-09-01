package com.restaurant.ordering.model;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.OptimisticLockType;
import org.hibernate.annotations.OptimisticLocking;

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
@DynamicUpdate
@OptimisticLocking(type = OptimisticLockType.ALL)
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

    /**
     * Returns the database-generated identifier for this user.
     *
     * @return user identifier
     */
    public Long getId() { return id; }

    /**
     * Returns the unique username associated with this account.
     *
     * @return account username
     */
    public String getUsername() { return username; }

    /**
     * Changes the account username.
     *
     * @param username new unique username
     */
    public void setUsername(String username) { this.username = username; }

    /**
     * Returns the encoded password stored for authentication.
     *
     * @return stored encoded password
     */
    public String getPassword() { return password; }

    /**
     * Changes the encoded password stored for this account.
     *
     * @param password encoded password to persist
     */
    public void setPassword(String password) { this.password = password; }

    /**
     * Returns the user's first name.
     *
     * @return user's first name
     */
    public String getFirstName() { return firstName; }

    /**
     * Changes the user's first name.
     *
     * @param firstName user's first name
     */
    public void setFirstName(String firstName) { this.firstName = firstName; }

    /**
     * Returns the user's last name.
     *
     * @return user's last name
     */
    public String getLastName() { return lastName; }

    /**
     * Changes the user's last name.
     *
     * @param lastName user's last name
     */
    public void setLastName(String lastName) { this.lastName = lastName; }

    /**
     * Returns the unique email address used as the authentication principal.
     *
     * @return unique account email address
     */
    public String getEmail() { return email; }

    /**
     * Changes the unique email address associated with this account.
     *
     * @param email unique account email address
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Returns the contact phone number stored for the user.
     *
     * @return contact phone number
     */
    public String getPhone() { return phone; }

    /**
     * Changes the user's contact phone number.
     *
     * @param phone contact phone number
     */
    public void setPhone(String phone) { this.phone = phone; }

    /**
     * Returns the authorization role assigned to this account.
     *
     * @return assigned application role
     */
    public Role getRole() { return role; }

    /**
     * Changes the authorization role assigned to this account.
     *
     * @param role authorization role to assign
     */
    public void setRole(Role role) { this.role = role; }

    /**
     * Indicates whether this account is currently allowed to authenticate.
     *
     * @return {@code true} when the account is enabled
     */
    public boolean isEnabled() { return enabled; }

    /**
     * Enables or disables authentication for this account.
     *
     * @param enabled whether authentication is permitted
     */
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    /**
     * Returns the timestamp at which this account was created.
     *
     * @return account creation timestamp
     */
    public LocalDateTime getCreatedAt() { return createdAt; }
}
