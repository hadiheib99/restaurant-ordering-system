package com.restaurant.ordering.repository;

import com.restaurant.ordering.model.Meal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for {@link Meal} entities and menu-specific queries.
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
public interface MealRepository extends JpaRepository<Meal, Long> {
    /** @return all meals currently marked available */
    List<Meal> findByAvailableTrue();
    /** @param categoryId category identifier @return meals belonging to the category */
    List<Meal> findByCategoryId(Long categoryId);
    /** @param name partial meal name @return case-insensitive matching meals */
    List<Meal> findByNameContainingIgnoreCase(String name);
    /** @param name exact meal name @return matching meal when present */
    Optional<Meal> findByNameIgnoreCase(String name);
    /** @param name exact meal name @return true when the name already exists */
    boolean existsByNameIgnoreCase(String name);
}
