package com.restaurant.ordering;

import com.restaurant.ordering.security.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.jms.annotation.EnableJms;

/**
 * Main entry point of the Restaurant Ordering System backend.
 *
 * <p>The application enables Spring Boot auto-configuration, JMS support and
 * strongly typed JWT configuration properties.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@EnableConfigurationProperties(JwtProperties.class)
@EnableJms
@SpringBootApplication
public class RestaurantOrderingSystemApplication {

    /**
     * Starts the Spring Boot application and initializes its application context.
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(RestaurantOrderingSystemApplication.class, args);
    }
}
