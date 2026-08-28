package com.restaurant.ordering.service;

import com.restaurant.ordering.exception.ResourceNotFoundException;
import com.restaurant.ordering.model.Category;
import com.restaurant.ordering.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CategoryServiceImpl} category-management behavior.
 *
 * <p>The suite verifies create, read, partial update, missing-resource handling
 * and deletion while isolating the service from the database with a mocked
 * {@link CategoryRepository}.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock private CategoryRepository categoryRepository;
    private CategoryServiceImpl service;

    /** Creates the category service with its mocked repository before every test. */
    @BeforeEach
    void setUp() {
        service = new CategoryServiceImpl(categoryRepository);
    }

    /** Verifies that creating a category delegates persistence to the repository. */
    @Test
    void createCategorySavesCategory() {
        Category category = new Category();
        category.setName("Pizza");
        when(categoryRepository.save(category)).thenReturn(category);

        assertSame(category, service.createCategory(category));
        verify(categoryRepository).save(category);
    }

    /** Verifies that retrieving all categories returns the repository data unchanged. */
    @Test
    void getAllCategoriesReturnsRepositoryData() {
        Category pizza = new Category();
        pizza.setName("Pizza");
        when(categoryRepository.findAll()).thenReturn(List.of(pizza));

        assertEquals(List.of(pizza), service.getAllCategories());
    }

    /** Verifies that a partial update changes supplied values while retaining omitted fields. */
    @Test
    void updateCategoryChangesOnlyProvidedFields() {
        Category existing = new Category();
        existing.setName("Old");
        existing.setDescription("Keep me");

        Category update = new Category();
        update.setName("New");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(existing)).thenReturn(existing);

        Category result = service.updateCategory(1L, update);

        assertEquals("New", result.getName());
        assertEquals("Keep me", result.getDescription());
    }

    /** Verifies that requesting an unknown category raises the domain not-found exception. */
    @Test
    void missingCategoryThrowsResourceNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getCategoryById(99L));
    }

    /** Verifies that an existing category can be deleted by identifier. */
    @Test
    void deleteCategoryRequiresExistingId() {
        when(categoryRepository.existsById(5L)).thenReturn(true);
        service.deleteCategory(5L);
        verify(categoryRepository).deleteById(5L);
    }
}
