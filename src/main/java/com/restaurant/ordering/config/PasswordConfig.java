package com.restaurant.ordering.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Provides password-security beans used by authentication and user management.
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@Configuration
public class PasswordConfig {

    /**
     * Creates the BCrypt encoder used before passwords are persisted.
     * @return BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
