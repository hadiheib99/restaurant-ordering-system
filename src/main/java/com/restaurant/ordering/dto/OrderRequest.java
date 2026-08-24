package com.restaurant.ordering.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Request DTO used to create a restaurant order.
 *
 * <p>The request identifies the customer, optionally assigns a waiter and carries
 * a validated list of requested meal items.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@Getter
@Setter
public class OrderRequest {

    /** Customer that owns the order. */
    @NotNull(message = "Customer ID is required")
    private Long customerId;

    /** Optional waiter assigned when orders are entered for restaurant customers. */
    private Long waiterId;

    /** Validated meal selections; an order must contain at least one item. */
    @NotEmpty(message = "The order must contain at least one item")
    private List<@Valid OrderItemRequest> items;
}
