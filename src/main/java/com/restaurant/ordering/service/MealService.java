package com.restaurant.ordering.service;

import com.restaurant.ordering.dto.MealRequest;
import com.restaurant.ordering.dto.MealResponse;
import java.util.List;

/**
 * Business-service contract for restaurant meals.
 *
 * <p>Defines menu CRUD operations together with category and availability queries.
 * DTOs are used so controllers do not expose JPA entities directly.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
public interface MealService {

    /**
     * Creates a new meal from client-supplied menu data.
     *
     * @param mealRequest data for the new meal
     * @return created meal
     */
    MealResponse createMeal(MealRequest mealRequest);

    /**
     * Retrieves one meal by its unique identifier.
     *
     * @param id unique meal identifier
     * @return matching meal
     */
    MealResponse getMealById(Long id);

    /**
     * Retrieves every meal stored in the restaurant menu.
     *
     * @return all meals in the restaurant menu
     */
    List<MealResponse> getAllMeals();

    /**
     * Retrieves meals associated with one category.
     *
     * @param categoryId category identifier
     * @return meals in the category
     */
    List<MealResponse> getMealsByCategory(Long categoryId);

    /**
     * Retrieves meals that are currently available for ordering.
     *
     * @return currently available meals
     */
    List<MealResponse> getAvailableMeals();

    /**
     * Updates the editable values of an existing meal.
     *
     * @param id unique meal identifier
     * @param mealRequest updated meal data
     * @return updated meal
     */
    MealResponse updateMeal(Long id, MealRequest mealRequest);

    /**
     * Deletes a meal by its identifier.
     *
     * @param id unique meal identifier
     */
    void deleteMeal(Long id);
}
