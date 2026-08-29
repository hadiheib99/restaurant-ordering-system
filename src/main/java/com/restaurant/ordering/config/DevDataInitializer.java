package com.restaurant.ordering.config;

import com.restaurant.ordering.model.Category;
import com.restaurant.ordering.model.Meal;
import com.restaurant.ordering.model.Role;
import com.restaurant.ordering.model.User;
import com.restaurant.ordering.repository.CategoryRepository;
import com.restaurant.ordering.repository.MealRepository;
import com.restaurant.ordering.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Seeds development/demo data when {@code app.seed-data} is enabled.
 *
 * <p>Demo-account passwords are supplied through environment-backed application
 * properties instead of being committed to source control. When seeding is
 * enabled, the initializer creates or refreshes the demo accounts so the local
 * credentials stored outside Git remain synchronized with the database.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed-data", havingValue = "true")
public class DevDataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final MealRepository mealRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-password:}")
    private String adminPassword;

    @Value("${app.seed.waiter-password:}")
    private String waiterPassword;

    @Value("${app.seed.customer-password:}")
    private String customerPassword;

    @Value("${app.seed.chef-password:}")
    private String chefPassword;

    /**
     * Executes all development-data initialization inside one transaction.
     *
     * @param args Spring Boot application arguments
     */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        validateSeedPasswords();
        seedUsers();
        seedMenu();
    }

    /** Creates or refreshes the default admin, waiter, customer and chef accounts. */
    private void seedUsers() {
        upsertDemoUser("admin", adminPassword, "Restaurant", "Manager", "admin@restaurant.com", "0501234567", Role.ADMIN);
        upsertDemoUser("waiter1", waiterPassword, "Daniel", "Cohen", "waiter1@restaurant.com", "0502222222", Role.WAITER);
        upsertDemoUser("customer1", customerPassword, "John", "Smith", "customer1@restaurant.com", "0501111111", Role.CUSTOMER);
        upsertDemoUser("chef1", chefPassword, "Kitchen", "Chef", "chef1@restaurant.com", "0503333333", Role.CHEF);
    }

    /**
     * Creates or refreshes one demonstration user with a password supplied outside Git.
     *
     * @param username demo username
     * @param password environment-backed demo password
     * @param firstName first name
     * @param lastName last name
     * @param email demo email address
     * @param phone demo phone number
     * @param role application role
     */
    private void upsertDemoUser(String username, String password, String firstName, String lastName,
                                String email, String phone, Role role) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> userRepository.findByUsernameIgnoreCase(username).orElseGet(User::new));

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

    /** Ensures that every seeded account has a password supplied by the environment. */
    private void validateSeedPasswords() {
        requireSeedPassword("SEED_ADMIN_PASSWORD", adminPassword);
        requireSeedPassword("SEED_WAITER_PASSWORD", waiterPassword);
        requireSeedPassword("SEED_CUSTOMER_PASSWORD", customerPassword);
        requireSeedPassword("SEED_CHEF_PASSWORD", chefPassword);
    }

    /**
     * Rejects demo-data startup when a required password is absent.
     *
     * @param environmentVariable environment variable that must be supplied
     * @param password resolved password value
     */
    private static void requireSeedPassword(String environmentVariable, String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                    environmentVariable + " must be set when APP_SEED_DATA=true"
            );
        }
    }

    /** Creates or refreshes the demonstration categories and meals. */
    private void seedMenu() {
        Category pizza = category("Pizza", "Freshly baked pizzas");
        Category burgers = category("Burgers", "Grilled burgers and sandwiches");
        Category pasta = category("Pasta", "Classic Italian pasta dishes");
        Category salads = category("Salads", "Fresh salads and lighter meals");
        Category drinks = category("Drinks", "Cold drinks and refreshments");
        Category desserts = category("Desserts", "Sweet finishes");

        meal("Margherita Pizza", "Tomato sauce, mozzarella and basil", "40.00", pizza,
                "https://images.unsplash.com/photo-1574071318508-1cdbab80d002?auto=format&fit=crop&w=900&q=80");
        meal("Pepperoni Pizza", "Tomato sauce, mozzarella and pepperoni", "48.00", pizza,
                "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?auto=format&fit=crop&w=900&q=80");
        meal("Mushroom Pizza", "Mozzarella, mushrooms, tomato sauce and oregano", "46.00", pizza,
                "https://images.unsplash.com/photo-1579751626657-72bc17010498?auto=format&fit=crop&w=900&q=80");
        meal("Classic Burger", "Beef patty, lettuce, tomato, onion and house sauce", "52.00", burgers,
                "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=900&q=80");
        meal("Crispy Chicken Burger", "Crispy chicken, lettuce, pickles and garlic sauce", "49.00", burgers,
                "https://images.unsplash.com/photo-1562967914-608f82629710?auto=format&fit=crop&w=900&q=80");
        meal("Spaghetti Bolognese", "Spaghetti with slow-cooked beef and tomato sauce", "54.00", pasta,
                "https://images.unsplash.com/photo-1473093295043-cdd812d0e601?auto=format&fit=crop&w=900&q=80");
        meal("Fettuccine Alfredo", "Creamy parmesan sauce with fettuccine", "51.00", pasta,
                "https://images.unsplash.com/photo-1556761223-4c4282c73f77?auto=format&fit=crop&w=900&q=80");
        meal("Caesar Salad", "Romaine lettuce, parmesan, croutons and Caesar dressing", "38.00", salads,
                "https://images.unsplash.com/photo-1546793665-c74683f339c1?auto=format&fit=crop&w=900&q=80");
        meal("Greek Salad", "Tomato, cucumber, olives, feta and olive oil", "36.00", salads,
                "https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&w=900&q=80");
        meal("Cola", "Chilled 330ml soft drink", "10.00", drinks,
                "https://images.unsplash.com/photo-1629203851122-3726ecdf080e?auto=format&fit=crop&w=900&q=80");
        meal("Fresh Lemonade", "Fresh lemon, mint and ice", "16.00", drinks, "/images/fresh-lemonade.svg");
        meal("Chocolate Cake", "Rich chocolate cake with chocolate sauce", "28.00", desserts,
                "https://images.unsplash.com/photo-1578985545062-69928b1d9587?auto=format&fit=crop&w=900&q=80");
        meal("Cheesecake", "Creamy cheesecake with berry topping", "30.00", desserts,
                "https://images.unsplash.com/photo-1524351199678-941a58a3df50?auto=format&fit=crop&w=900&q=80");
    }

    /** Finds a category by name or creates it when missing. */
    private Category category(String name, String description) {
        return categoryRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            Category category = new Category();
            category.setName(name);
            category.setDescription(description);
            return categoryRepository.save(category);
        });
    }

    /** Creates or refreshes one demonstration meal with the supplied menu data. */
    private void meal(String name, String description, String price, Category category, String imageUrl) {
        Meal meal = mealRepository.findByNameIgnoreCase(name).orElseGet(Meal::new);
        meal.setName(name);
        meal.setDescription(description);
        meal.setPrice(new BigDecimal(price));
        meal.setAvailable(true);
        meal.setCategory(category);
        meal.setImageUrl(imageUrl);
        mealRepository.save(meal);
    }
}
