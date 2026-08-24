package com.restaurant.ordering.repository;

import com.restaurant.ordering.model.Role;
import com.restaurant.ordering.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for user accounts and authentication-oriented lookups.
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /** @param username account username @return matching user ignoring case */
    Optional<User> findByUsernameIgnoreCase(String username);
    /** @param email account email @return matching user ignoring case */
    Optional<User> findByEmailIgnoreCase(String email);
    /** @param username account username @return whether the username exists ignoring case */
    boolean existsByUsernameIgnoreCase(String username);
    /** @param email account email @return whether the email exists ignoring case */
    boolean existsByEmailIgnoreCase(String email);
    /** @param role application role @return users assigned to that role */
    List<User> findByRole(Role role);
    /** @param email exact email principal @return matching user when present */
    Optional<User> findByEmail(String email);
    /** @param email exact email @return whether an account has the email */
    boolean existsByEmail(String email);
}
