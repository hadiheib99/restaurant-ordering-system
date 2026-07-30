package com.restaurant.ordering.service;

import com.restaurant.ordering.dto.MealRequest;
import com.restaurant.ordering.dto.MealResponse;
import com.restaurant.ordering.exception.ResourceNotFoundException;
import com.restaurant.ordering.model.Category;
import com.restaurant.ordering.model.Meal;
import com.restaurant.ordering.repository.MealRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MealServiceImpl implements MealService {

    @Autowired
    private MealRepository mealRepository;

    @Autowired
    private CategoryService categoryService;

    @Override
    public MealResponse createMeal(MealRequest mealRequest) {
        Category category = categoryService.getCategoryById(mealRequest.getCategoryId());

        Meal meal = new Meal();
        meal.setName(mealRequest.getName());
        meal.setDescription(mealRequest.getDescription());
        meal.setPrice(mealRequest.getPrice());
        meal.setCategory(category);
        meal.setAvailable(mealRequest.getAvailable() != null ? mealRequest.getAvailable() : true);

        Meal savedMeal = mealRepository.save(meal);
        return convertToResponse(savedMeal);
    }

    @Override
    public MealResponse getMealById(Long id) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meal not found with id: " + id));
        return convertToResponse(meal);
    }

    @Override
    public List<MealResponse> getAllMeals() {
        return mealRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MealResponse> getMealsByCategory(Long categoryId) {
        return mealRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MealResponse> getAvailableMeals() {
        return mealRepository.findByAvailableTrue()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MealResponse updateMeal(Long id, MealRequest mealRequest) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meal not found with id: " + id));

        if (mealRequest.getName() != null) {
            meal.setName(mealRequest.getName());
        }
        if (mealRequest.getDescription() != null) {
            meal.setDescription(mealRequest.getDescription());
        }
        if (mealRequest.getPrice() != null) {
            meal.setPrice(mealRequest.getPrice());
        }
        if (mealRequest.getCategoryId() != null) {
            Category category = categoryService.getCategoryById(mealRequest.getCategoryId());
            meal.setCategory(category);
        }
        if (mealRequest.getAvailable() != null) {
            meal.setAvailable(mealRequest.getAvailable());
        }

        Meal updatedMeal = mealRepository.save(meal);
        return convertToResponse(updatedMeal);
    }

    @Override
    public void deleteMeal(Long id) {
        if (!mealRepository.existsById(id)) {
            throw new ResourceNotFoundException("Meal not found with id: " + id);
        }
        mealRepository.deleteById(id);
    }

    private MealResponse convertToResponse(Meal meal) {
        return new MealResponse(
                meal.getId(),
                meal.getName(),
                meal.getDescription(),
                meal.getPrice(),
                meal.getCategory().getId(),
                meal.getCategory().getName(),
                meal.getAvailable()
        );
    }
}

