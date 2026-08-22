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

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock private CategoryRepository categoryRepository;
    private CategoryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CategoryServiceImpl();
        var field = CategoryServiceImpl.class.getDeclaredFields()[0];
        field.setAccessible(true);
        try {
            field.set(service, categoryRepository);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void createCategorySavesCategory() {
        Category category = new Category();
        category.setName("Pizza");
        when(categoryRepository.save(category)).thenReturn(category);

        assertSame(category, service.createCategory(category));
        verify(categoryRepository).save(category);
    }

    @Test
    void getAllCategoriesReturnsRepositoryData() {
        Category pizza = new Category();
        pizza.setName("Pizza");
        when(categoryRepository.findAll()).thenReturn(List.of(pizza));

        assertEquals(List.of(pizza), service.getAllCategories());
    }

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

    @Test
    void missingCategoryThrowsResourceNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getCategoryById(99L));
    }

    @Test
    void deleteCategoryRequiresExistingId() {
        when(categoryRepository.existsById(5L)).thenReturn(true);
        service.deleteCategory(5L);
        verify(categoryRepository).deleteById(5L);
    }
}
