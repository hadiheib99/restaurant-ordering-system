package com.restaurant.ordering.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

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
}
