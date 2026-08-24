package com.restaurant.ordering.service;

import com.restaurant.ordering.model.Category;
import java.util.List;

/**
 * Business-service contract for restaurant meal categories.
 *
 * <p>The interface separates REST controllers from persistence details and
 * defines the CRUD operations supported for {@link Category} entities.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
public interface CategoryService {

    /**
     * Persists a new category.
     * @param category category to create
     * @return persisted category
     */
    Category createCategory(Category category);

    /**
     * Finds a category by identifier.
     * @param id unique category identifier
     * @return matching category
     */
    Category getCategoryById(Long id);

    /** @return every persisted category */
    List<Category> getAllCategories();

    /**
     * Updates an existing category.
     * @param id unique category identifier
     * @param category new category values
     * @return updated category
     */
    Category updateCategory(Long id, Category category);

    /**
     * Deletes a category.
     * @param id unique category identifier
     */
    void deleteCategory(Long id);
}
