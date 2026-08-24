package com.restaurant.ordering.security.service;

import com.restaurant.ordering.model.User;
import com.restaurant.ordering.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Adapts restaurant user records to Spring Security {@link UserDetails}.
 *
 * <p>Email is used as the authentication username and the persisted role is
 * converted to a Spring Security role authority.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads security credentials and authorities for an account email.
     * @param email email supplied as the authentication principal
     * @return Spring Security user details
     * @throws UsernameNotFoundException when no account has the supplied email
     */
    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}
