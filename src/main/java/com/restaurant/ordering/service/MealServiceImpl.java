package com.restaurant.ordering.service;

import com.restaurant.ordering.dto.MealRequest;
import com.restaurant.ordering.dto.MealResponse;
import com.restaurant.ordering.exception.ResourceNotFoundException;
import com.restaurant.ordering.model.Category;
import com.restaurant.ordering.model.Meal;
import com.restaurant.ordering.repository.MealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Transactional implementation of {@link MealService}.
 *
 * <p>The service validates referenced categories, maps incoming DTOs to JPA
 * entities, persists meals and converts entities back to response DTOs.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class MealServiceImpl implements MealService {

    private final MealRepository mealRepository;
    private final CategoryService categoryService;

    /** {@inheritDoc} */
    @Override
    public MealResponse createMeal(MealRequest mealRequest) {
        Category category = categoryService.getCategoryById(mealRequest.getCategoryId());

        Meal meal = new Meal();
        meal.setName(mealRequest.getName());
        meal.setDescription(mealRequest.getDescription());
        meal.setPrice(mealRequest.getPrice());
        meal.setCategory(category);
        meal.setAvailable(mealRequest.getAvailable() != null ? mealRequest.getAvailable() : true);
        meal.setImageUrl(mealRequest.getImageUrl());

        return convertToResponse(mealRepository.save(meal));
    }

    /**
     * {@inheritDoc}
     * @throws ResourceNotFoundException when the meal does not exist
     */
    @Override
    @Transactional(readOnly = true)
    public MealResponse getMealById(Long id) {
        return convertToResponse(findMeal(id));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<MealResponse> getAllMeals() {
        return mealRepository.findAll().stream().map(this::convertToResponse).toList();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<MealResponse> getMealsByCategory(Long categoryId) {
        return mealRepository.findByCategoryId(categoryId).stream().map(this::convertToResponse).toList();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<MealResponse> getAvailableMeals() {
        return mealRepository.findByAvailableTrue().stream().map(this::convertToResponse).toList();
    }

    /**
     * {@inheritDoc}
     * <p>Only non-null request fields replace existing entity values.</p>
     * @throws ResourceNotFoundException when the meal or requested category does not exist
     */
    @Override
    public MealResponse updateMeal(Long id, MealRequest mealRequest) {
        Meal meal = findMeal(id);

        if (mealRequest.getName() != null) meal.setName(mealRequest.getName());
        if (mealRequest.getDescription() != null) meal.setDescription(mealRequest.getDescription());
        if (mealRequest.getPrice() != null) meal.setPrice(mealRequest.getPrice());
        if (mealRequest.getCategoryId() != null) meal.setCategory(categoryService.getCategoryById(mealRequest.getCategoryId()));
        if (mealRequest.getAvailable() != null) meal.setAvailable(mealRequest.getAvailable());
        if (mealRequest.getImageUrl() != null) meal.setImageUrl(mealRequest.getImageUrl());

        return convertToResponse(mealRepository.save(meal));
    }

    /**
     * {@inheritDoc}
     * @throws ResourceNotFoundException when the meal does not exist
     */
    @Override
    public void deleteMeal(Long id) {
        mealRepository.delete(findMeal(id));
    }

    /**
     * Loads a meal entity or fails with a domain-friendly exception.
     * @param id unique meal identifier
     * @return persistent meal entity
     * @throws ResourceNotFoundException when no matching meal exists
     */
    private Meal findMeal(Long id) {
        return mealRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meal not found with id: " + id));
    }

    /**
     * Converts a persistent meal entity into the REST response representation.
     * @param meal persistent meal entity
     * @return meal response DTO including category information
     */
    private MealResponse convertToResponse(Meal meal) {
        return new MealResponse(
                meal.getId(),
                meal.getName(),
                meal.getDescription(),
                meal.getPrice(),
                meal.getCategory().getId(),
                meal.getCategory().getName(),
                meal.getAvailable(),
                meal.getImageUrl()
        );
    }
}
