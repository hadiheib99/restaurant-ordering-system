package com.restaurant.ordering.repository;

import com.restaurant.ordering.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data repository for {@link Category} persistence and name-based lookup.
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Checks whether a category with the supplied name already exists.
     *
     * @param name category name to check
     * @return {@code true} when the name already exists, ignoring letter case
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Finds a category by its name without considering letter case.
     *
     * @param name category name to search for
     * @return optional containing the matching category when present
     */
    Optional<Category> findByNameIgnoreCase(String name);
}
