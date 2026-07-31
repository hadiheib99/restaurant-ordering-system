package com.restaurant.ordering.security.config;

import com.restaurant.ordering.security.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;



    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authenticationProvider(authenticationProvider)

                .authorizeHttpRequests(auth -> auth

                        // Login is public
                        .requestMatchers("/api/auth/**").permitAll()

                        // Anyone can view the menu
                        .requestMatchers(HttpMethod.GET, "/api/meals/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()

                        // Only ADMIN can manage users
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // ADMIN manages meals and categories
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/meals/**",
                                "/api/categories/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/meals/**",
                                "/api/categories/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/meals/**",
                                "/api/categories/**"
                        ).hasRole("ADMIN")

                        // All logged-in roles may view orders for now
                        .requestMatchers(HttpMethod.GET, "/api/orders/**")
                        .hasAnyRole("ADMIN", "WAITER", "CUSTOMER")

                        // Waiters, customers, and admins may create orders
                        .requestMatchers(HttpMethod.POST, "/api/orders/**")
                        .hasAnyRole("ADMIN", "WAITER", "CUSTOMER")

                        // Waiters and admins may update an order
                        .requestMatchers(HttpMethod.PUT, "/api/orders/**")
                        .hasAnyRole("ADMIN", "WAITER")

                        // Only ADMIN may delete orders
                        .requestMatchers(HttpMethod.DELETE, "/api/orders/**")
                        .hasRole("ADMIN")

                        // Everything else requires login
                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(
                                        jwtAuthenticationConverter()
                                )
                        )
                );

        return http.build();
    }
    @Bean
    public Converter<Jwt, ? extends AbstractAuthenticationToken>
    jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName("role");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter authenticationConverter =
                new JwtAuthenticationConverter();

        authenticationConverter.setJwtGrantedAuthoritiesConverter(
                authoritiesConverter
        );

        return authenticationConverter;
    }
}