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

    /**
     * Retrieves every meal currently marked as available.
     *
     * @return meals that customers may currently order
     */
    List<Meal> findByAvailableTrue();

    /**
     * Retrieves meals that belong to one category.
     *
     * @param categoryId category identifier
     * @return meals belonging to the specified category
     */
    List<Meal> findByCategoryId(Long categoryId);

    /**
     * Finds a meal by its exact name without considering case.
     *
     * @param name exact meal name
     * @return optional containing the matching meal when present
     */
    Optional<Meal> findByNameIgnoreCase(String name);

    /**
     * Checks whether a meal with the supplied name already exists.
     *
     * @param name exact meal name
     * @return {@code true} when the name already exists, ignoring letter case
     */
    boolean existsByNameIgnoreCase(String name);
}
