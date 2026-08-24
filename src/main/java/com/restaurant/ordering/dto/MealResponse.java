package com.restaurant.ordering.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Response DTO returned to clients for menu meals.
 *
 * <p>Includes display-ready category information while keeping persistence
 * relationships and Hibernate internals outside the REST representation.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MealResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Long categoryId;
    private String categoryName;
    private Boolean available;
    private String imageUrl;
}
