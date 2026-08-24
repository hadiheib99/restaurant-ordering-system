package com.restaurant.ordering.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity representing a menu category such as Pizza, Drinks or Desserts.
 *
 * <p>Each category has a unique display name and an optional description. Meals
 * reference a category to support menu grouping and filtering.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    /** Database-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique category name displayed in the menu. */
    @Column(nullable = false, unique = true)
    private String name;

    /** Human-readable explanation of the category. */
    private String description;
}
