package com.restaurant.ordering.service;

import com.restaurant.ordering.dto.MealRequest;
import com.restaurant.ordering.exception.ResourceNotFoundException;
import com.restaurant.ordering.model.Category;
import com.restaurant.ordering.model.Meal;
import com.restaurant.ordering.repository.MealRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MealServiceImplTest {

    @Mock private MealRepository mealRepository;
    @Mock private CategoryService categoryService;
    private MealServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MealServiceImpl(mealRepository, categoryService);
    }

    @Test
    void createMealUsesCategoryAndDefaultsAvailabilityToTrue() {
        Category category = category(2L, "Pizza");
        MealRequest request = new MealRequest("Margherita", "Classic", new BigDecimal("40.00"), 2L, null);

        when(categoryService.getCategoryById(2L)).thenReturn(category);
        when(mealRepository.save(any(Meal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.createMeal(request);

        assertEquals("Margherita", response.getName());
        assertEquals(new BigDecimal("40.00"), response.getPrice());
        assertEquals("Pizza", response.getCategoryName());
        assertTrue(response.getAvailable());
    }

    @Test
    void getAvailableMealsMapsEntitiesToResponses() {
        Category category = category(1L, "Drinks");
        Meal meal = meal("Cola", category, true);
        when(mealRepository.findByAvailableTrue()).thenReturn(List.of(meal));

        var result = service.getAvailableMeals();

        assertEquals(1, result.size());
        assertEquals("Cola", result.getFirst().getName());
        assertEquals("Drinks", result.getFirst().getCategoryName());
    }

    @Test
    void updateMealUpdatesOnlyProvidedValues() {
        Category category = category(1L, "Pizza");
        Meal existing = meal("Old", category, true);
        existing.setDescription("Keep description");
        existing.setPrice(new BigDecimal("35.00"));

        MealRequest request = new MealRequest("New name", null, new BigDecimal("42.00"), null, false);
        when(mealRepository.findById(4L)).thenReturn(Optional.of(existing));
        when(mealRepository.save(existing)).thenReturn(existing);

        var result = service.updateMeal(4L, request);

        assertEquals("New name", result.getName());
        assertEquals("Keep description", result.getDescription());
        assertEquals(new BigDecimal("42.00"), result.getPrice());
        assertFalse(result.getAvailable());
    }

    @Test
    void missingMealThrowsResourceNotFound() {
        when(mealRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getMealById(99L));
    }

    private static Category category(Long id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        return category;
    }

    private static Meal meal(String name, Category category, boolean available) {
        Meal meal = new Meal();
        meal.setName(name);
        meal.setDescription("Description");
        meal.setPrice(new BigDecimal("10.00"));
        meal.setCategory(category);
        meal.setAvailable(available);
        return meal;
    }
}
