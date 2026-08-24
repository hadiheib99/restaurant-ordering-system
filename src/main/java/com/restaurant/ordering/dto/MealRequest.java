package com.restaurant.ordering.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Request DTO used when creating or updating a meal.
 *
 * <p>The DTO carries editable menu fields and references a category by id rather
 * than exposing the JPA category entity through the REST API.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MealRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private Long categoryId;
    private Boolean available;
    private String imageUrl;

    /**
     * Compatibility constructor for meal requests without an image URL.
     * @param name meal name
     * @param description meal description
     * @param price menu price
     * @param categoryId category identifier
     * @param available availability flag
     */
    public MealRequest(String name, String description, BigDecimal price, Long categoryId, Boolean available) {
        this(name, description, price, categoryId, available, null);
    }
}
