package com.restaurant.ordering.config;

import com.restaurant.ordering.model.Category;
import com.restaurant.ordering.model.Meal;
import com.restaurant.ordering.model.Role;
import com.restaurant.ordering.model.User;
import com.restaurant.ordering.repository.CategoryRepository;
import com.restaurant.ordering.repository.MealRepository;
import com.restaurant.ordering.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "app.seed-data",
        havingValue = "true",
        matchIfMissing = true
)
public class DevDataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final MealRepository mealRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedUsers();
        seedMenu();
    }

    private void seedUsers() {
        createUserIfMissing(
                "admin",
                "Admin123",
                "Restaurant",
                "Manager",
                "admin@restaurant.com",
                "0501234567",
                Role.ADMIN
        );

        createUserIfMissing(
                "waiter1",
                "Waiter123",
                "Daniel",
                "Cohen",
                "waiter1@restaurant.com",
                "0502222222",
                Role.WAITER
        );

        createUserIfMissing(
                "customer1",
                "Customer123",
                "John",
                "Smith",
                "customer1@restaurant.com",
                "0501111111",
                Role.CUSTOMER
        );

        createUserIfMissing(
                "chef1",
                "Chef123",
                "Kitchen",
                "Chef",
                "chef1@restaurant.com",
                "0503333333",
                Role.CHEF
        );
    }

    private void createUserIfMissing(
            String username,
            String password,
            String firstName,
            String lastName,
            String email,
            String phone,
            Role role
    ) {
        if (userRepository.existsByEmailIgnoreCase(email) ||
                userRepository.existsByUsernameIgnoreCase(username)) {
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(role);
        user.setEnabled(true);
        userRepository.save(user);
    }

    private void seedMenu() {
        if (categoryRepository.count() > 0 || mealRepository.count() > 0) {
            return;
        }

        Category pizza = new Category();
        pizza.setName("Pizza");
        pizza.setDescription("Freshly baked pizzas");
        pizza = categoryRepository.save(pizza);

        Meal margherita = new Meal();
        margherita.setName("Margherita Pizza");
        margherita.setDescription("Tomato sauce, mozzarella and basil");
        margherita.setPrice(new BigDecimal("40.00"));
        margherita.setAvailable(true);
        margherita.setCategory(pizza);
        mealRepository.save(margherita);
    }
}
