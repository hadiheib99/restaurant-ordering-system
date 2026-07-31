package com.restaurant.ordering;

import com.restaurant.ordering.security.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.jms.annotation.EnableJms;
@EnableConfigurationProperties(JwtProperties.class)

@EnableJms
@SpringBootApplication
public class RestaurantOrderingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                RestaurantOrderingSystemApplication.class,
                args
        );
    }
}