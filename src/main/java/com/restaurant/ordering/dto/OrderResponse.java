package com.restaurant.ordering.dto;

import com.restaurant.ordering.model.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO representing a complete order returned by the REST API.
 *
 * <p>The object combines customer/waiter display information, lifecycle status,
 * monetary total, timestamps and line items without exposing JPA entities.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@Getter
@Builder
public class OrderResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private Long waiterId;
    private String waiterName;
    private OrderStatus status;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResponse> items;
}
