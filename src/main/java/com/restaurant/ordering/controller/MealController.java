package com.restaurant.ordering.controller;

import com.restaurant.ordering.dto.MealRequest;
import com.restaurant.ordering.dto.MealResponse;
import com.restaurant.ordering.service.MealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/meals")
public class MealController {

    @Autowired
    private MealService mealService;

    @PostMapping
    public ResponseEntity<MealResponse> createMeal(@RequestBody MealRequest mealRequest) {
        return new ResponseEntity<>(mealService.createMeal(mealRequest), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MealResponse> getMealById(@PathVariable Long id) {
        return new ResponseEntity<>(mealService.getMealById(id), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<MealResponse>> getAllMeals() {
        return new ResponseEntity<>(mealService.getAllMeals(), HttpStatus.OK);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<MealResponse>> getMealsByCategory(@PathVariable Long categoryId) {
        return new ResponseEntity<>(mealService.getMealsByCategory(categoryId), HttpStatus.OK);
    }

    @GetMapping("/available")
    public ResponseEntity<List<MealResponse>> getAvailableMeals() {
        return new ResponseEntity<>(mealService.getAvailableMeals(), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MealResponse> updateMeal(@PathVariable Long id, @RequestBody MealRequest mealRequest) {
        return new ResponseEntity<>(mealService.updateMeal(id, mealRequest), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeal(@PathVariable Long id) {
        mealService.deleteMeal(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

