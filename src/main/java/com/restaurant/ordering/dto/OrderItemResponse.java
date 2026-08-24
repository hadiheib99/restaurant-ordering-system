package com.restaurant.ordering.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Response DTO describing a single line item in an order.
 *
 * <p>Contains the historical unit price and calculated subtotal used for order
 * displays, receipts and XML exports.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@Getter
@Builder
public class OrderItemResponse {
    private Long id;
    private Long mealId;
    private String mealName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
