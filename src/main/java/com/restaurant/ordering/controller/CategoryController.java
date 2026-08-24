package com.restaurant.ordering.controller;

import com.restaurant.ordering.model.Category;
import com.restaurant.ordering.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller responsible for category management.
 *
 * <p>Categories group meals into sections such as Pizza, Burgers or Drinks.
 * The controller exposes CRUD endpoints and delegates the business logic to
 * {@link CategoryService}.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * Creates a new meal category.
     *
     * @param category category information received from the client
     * @return HTTP 201 response containing the created category
     */
    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        return new ResponseEntity<>(categoryService.createCategory(category), HttpStatus.CREATED);
    }

    /**
     * Retrieves a category by its identifier.
     *
     * @param id unique category identifier
     * @return HTTP 200 response containing the requested category
     */
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return new ResponseEntity<>(categoryService.getCategoryById(id), HttpStatus.OK);
    }

    /**
     * Retrieves all available meal categories.
     *
     * @return HTTP 200 response containing all categories
     */
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return new ResponseEntity<>(categoryService.getAllCategories(), HttpStatus.OK);
    }

    /**
     * Updates an existing category.
     *
     * @param id unique category identifier
     * @param category replacement category data
     * @return HTTP 200 response containing the updated category
     */
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        return new ResponseEntity<>(categoryService.updateCategory(id, category), HttpStatus.OK);
    }

    /**
     * Deletes a category by its identifier.
     *
     * @param id unique category identifier
     * @return HTTP 204 response after successful deletion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
