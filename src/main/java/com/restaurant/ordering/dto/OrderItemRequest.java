package com.restaurant.ordering.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO representing one meal selection inside a new order.
 *
 * <p>Validation guarantees that every item references a meal and contains
 * between one and five units.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@Getter
@Setter
public class OrderItemRequest {

    /** Identifier of the meal selected by the customer. */
    @NotNull(message = "Meal ID is required")
    private Long mealId;

    /** Number of units requested for the meal; limited to five per item. */
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 5, message = "Quantity cannot exceed 5 per item")
    private Integer quantity;
}
