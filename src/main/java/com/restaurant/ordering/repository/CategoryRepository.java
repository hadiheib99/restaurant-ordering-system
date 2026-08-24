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
    /** @param name category name @return true when the name already exists, ignoring case */
    boolean existsByNameIgnoreCase(String name);

    /** @param name category name @return matching category when present */
    Optional<Category> findByNameIgnoreCase(String name);
}
