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
        createUserIfMissing("admin", "Admin123", "Restaurant", "Manager", "admin@restaurant.com", "0501234567", Role.ADMIN);
        createUserIfMissing("waiter1", "Waiter123", "Daniel", "Cohen", "waiter1@restaurant.com", "0502222222", Role.WAITER);
        createUserIfMissing("customer1", "Customer123", "John", "Smith", "customer1@restaurant.com", "0501111111", Role.CUSTOMER);
        createUserIfMissing("chef1", "Chef123", "Kitchen", "Chef", "chef1@restaurant.com", "0503333333", Role.CHEF);
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
        if (userRepository.existsByEmailIgnoreCase(email) || userRepository.existsByUsernameIgnoreCase(username)) {
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
        Category pizza = category("Pizza", "Freshly baked pizzas");
        Category burgers = category("Burgers", "Grilled burgers and sandwiches");
        Category pasta = category("Pasta", "Classic Italian pasta dishes");
        Category salads = category("Salads", "Fresh salads and lighter meals");
        Category drinks = category("Drinks", "Cold drinks and refreshments");
        Category desserts = category("Desserts", "Sweet finishes");

        meal("Margherita Pizza", "Tomato sauce, mozzarella and basil", "40.00", pizza);
        meal("Pepperoni Pizza", "Tomato sauce, mozzarella and pepperoni", "48.00", pizza);
        meal("Mushroom Pizza", "Mozzarella, mushrooms, tomato sauce and oregano", "46.00", pizza);
        meal("Classic Burger", "Beef patty, lettuce, tomato, onion and house sauce", "52.00", burgers);
        meal("Crispy Chicken Burger", "Crispy chicken, lettuce, pickles and garlic sauce", "49.00", burgers);
        meal("Spaghetti Bolognese", "Spaghetti with slow-cooked beef and tomato sauce", "54.00", pasta);
        meal("Fettuccine Alfredo", "Creamy parmesan sauce with fettuccine", "51.00", pasta);
        meal("Caesar Salad", "Romaine lettuce, parmesan, croutons and Caesar dressing", "38.00", salads);
        meal("Greek Salad", "Tomato, cucumber, olives, feta and olive oil", "36.00", salads);
        meal("Cola", "Chilled 330ml soft drink", "10.00", drinks);
        meal("Fresh Lemonade", "Fresh lemon, mint and ice", "16.00", drinks);
        meal("Chocolate Cake", "Rich chocolate cake with chocolate sauce", "28.00", desserts);
        meal("Cheesecake", "Creamy cheesecake with berry topping", "30.00", desserts);
    }

    private Category category(String name, String description) {
        return categoryRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    Category category = new Category();
                    category.setName(name);
                    category.setDescription(description);
                    return categoryRepository.save(category);
                });
    }

    private void meal(String name, String description, String price, Category category) {
        if (mealRepository.existsByNameIgnoreCase(name)) {
            return;
        }

        Meal meal = new Meal();
        meal.setName(name);
        meal.setDescription(description);
        meal.setPrice(new BigDecimal(price));
        meal.setAvailable(true);
        meal.setCategory(category);
        mealRepository.save(meal);
    }
}
