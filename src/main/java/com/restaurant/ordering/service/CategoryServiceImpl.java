package com.restaurant.ordering.service;

import com.restaurant.ordering.exception.ResourceNotFoundException;
import com.restaurant.ordering.model.Category;
import com.restaurant.ordering.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Default implementation of {@link CategoryService}.
 *
 * <p>Coordinates category CRUD operations with {@link CategoryRepository} and
 * converts missing database records into {@link ResourceNotFoundException}.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    /** {@inheritDoc} */
    @Override
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    /**
     * {@inheritDoc}
     * @throws ResourceNotFoundException when the category does not exist
     */
    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    /** {@inheritDoc} */
    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * {@inheritDoc}
     * <p>Only non-null values supplied by the caller replace existing values.</p>
     * @throws ResourceNotFoundException when the category does not exist
     */
    @Override
    public Category updateCategory(Long id, Category category) {
        Category existingCategory = getCategoryById(id);
        if (category.getName() != null) {
            existingCategory.setName(category.getName());
        }
        if (category.getDescription() != null) {
            existingCategory.setDescription(category.getDescription());
        }
        return categoryRepository.save(existingCategory);
    }

    /**
     * {@inheritDoc}
     * @throws ResourceNotFoundException when the category does not exist
     */
    @Override
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
