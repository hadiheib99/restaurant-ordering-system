package com.restaurant.ordering.repository;

import com.restaurant.ordering.model.Meal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MealRepository extends JpaRepository<Meal, Long> {

    List<Meal> findByAvailableTrue();

    List<Meal> findByCategoryId(Long categoryId);

    List<Meal> findByNameContainingIgnoreCase(String name);
}