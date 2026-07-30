package com.restaurant.ordering.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

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