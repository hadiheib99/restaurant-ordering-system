package com.restaurant.ordering.service;

import com.restaurant.ordering.dto.MealRequest;
import com.restaurant.ordering.dto.MealResponse;
import java.util.List;

public interface MealService {
    MealResponse createMeal(MealRequest mealRequest);
    MealResponse getMealById(Long id);
    List<MealResponse> getAllMeals();
    List<MealResponse> getMealsByCategory(Long categoryId);
    List<MealResponse> getAvailableMeals();
    MealResponse updateMeal(Long id, MealRequest mealRequest);
    void deleteMeal(Long id);
}

