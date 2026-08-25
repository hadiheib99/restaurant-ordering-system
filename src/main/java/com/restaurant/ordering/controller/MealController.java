package com.restaurant.ordering.controller;

import com.restaurant.ordering.dto.MealRequest;
import com.restaurant.ordering.dto.MealResponse;
import com.restaurant.ordering.service.MealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller responsible for restaurant meal management.
 *
 * <p>The controller exposes endpoints for creating, reading, updating and deleting
 * meals, as well as queries for meals by category and availability. Business logic
 * is delegated to {@link MealService}.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@RestController
@RequestMapping("/api/meals")
public class MealController {

    @Autowired
    private MealService mealService;

    /**
     * Creates a new meal in the restaurant menu.
     *
     * @param mealRequest meal data supplied by the client
     * @return HTTP 201 response containing the created meal
     */
    @PostMapping
    public ResponseEntity<MealResponse> createMeal(@RequestBody MealRequest mealRequest) {
        return new ResponseEntity<>(mealService.createMeal(mealRequest), HttpStatus.CREATED);
    }

    /**
     * Retrieves a meal by its unique identifier.
     *
     * @param id unique meal identifier
     * @return HTTP 200 response containing the requested meal
     */
    @GetMapping("/{id}")
    public ResponseEntity<MealResponse> getMealById(@PathVariable Long id) {
        return new ResponseEntity<>(mealService.getMealById(id), HttpStatus.OK);
    }

    /**
     * Retrieves every meal currently stored in the restaurant menu.
     *
     * @return HTTP 200 response containing all meals
     */
    @GetMapping
    public ResponseEntity<List<MealResponse>> getAllMeals() {
        return new ResponseEntity<>(mealService.getAllMeals(), HttpStatus.OK);
    }

    /**
     * Retrieves all meals that belong to one category.
     *
     * @param categoryId unique category identifier
     * @return HTTP 200 response containing matching meals
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<MealResponse>> getMealsByCategory(@PathVariable Long categoryId) {
        return new ResponseEntity<>(mealService.getMealsByCategory(categoryId), HttpStatus.OK);
    }

    /**
     * Retrieves meals that customers may currently order.
     *
     * @return HTTP 200 response containing available meals
     */
    @GetMapping("/available")
    public ResponseEntity<List<MealResponse>> getAvailableMeals() {
        return new ResponseEntity<>(mealService.getAvailableMeals(), HttpStatus.OK);
    }

    /**
     * Replaces editable data of an existing meal.
     *
     * @param id unique meal identifier
     * @param mealRequest updated meal data
     * @return HTTP 200 response containing the updated meal
     */
    @PutMapping("/{id}")
    public ResponseEntity<MealResponse> updateMeal(@PathVariable Long id, @RequestBody MealRequest mealRequest) {
        return new ResponseEntity<>(mealService.updateMeal(id, mealRequest), HttpStatus.OK);
    }

    /**
     * Deletes a meal from the menu.
     *
     * @param id unique meal identifier
     * @return HTTP 204 response after successful deletion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeal(@PathVariable Long id) {
        mealService.deleteMeal(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
