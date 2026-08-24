package com.restaurant.ordering.dto;

import com.restaurant.ordering.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Validated request DTO used by administrators to create or update user accounts.
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
public class UserRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    private String username;

    /** Optional on updates; when supplied it is encoded before persistence. */
    @Size(max = 100)
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    private String phone;

    @NotNull(message = "Role is required")
    private Role role;

    /** @return requested username */
    public String getUsername() { return username; }
    /** @param username requested username */
    public void setUsername(String username) { this.username = username; }
    /** @return optional plain-text password before service-layer encoding */
    public String getPassword() { return password; }
    /** @param password optional replacement password */
    public void setPassword(String password) { this.password = password; }
    /** @return first name */
    public String getFirstName() { return firstName; }
    /** @param firstName first name */
    public void setFirstName(String firstName) { this.firstName = firstName; }
    /** @return last name */
    public String getLastName() { return lastName; }
    /** @param lastName last name */
    public void setLastName(String lastName) { this.lastName = lastName; }
    /** @return account email */
    public String getEmail() { return email; }
    /** @param email account email */
    public void setEmail(String email) { this.email = email; }
    /** @return contact phone */
    public String getPhone() { return phone; }
    /** @param phone contact phone */
    public void setPhone(String phone) { this.phone = phone; }
    /** @return requested application role */
    public Role getRole() { return role; }
    /** @param role application role */
    public void setRole(Role role) { this.role = role; }
}
