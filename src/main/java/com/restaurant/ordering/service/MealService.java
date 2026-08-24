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

    /** @param mealRequest data for the new meal @return created meal */
    MealResponse createMeal(MealRequest mealRequest);

    /** @param id unique meal identifier @return matching meal */
    MealResponse getMealById(Long id);

    /** @return all meals in the restaurant menu */
    List<MealResponse> getAllMeals();

    /** @param categoryId category identifier @return meals in the category */
    List<MealResponse> getMealsByCategory(Long categoryId);

    /** @return meals currently available for ordering */
    List<MealResponse> getAvailableMeals();

    /** @param id unique meal identifier @param mealRequest updated meal data @return updated meal */
    MealResponse updateMeal(Long id, MealRequest mealRequest);

    /** @param id unique meal identifier */
    void deleteMeal(Long id);
}
