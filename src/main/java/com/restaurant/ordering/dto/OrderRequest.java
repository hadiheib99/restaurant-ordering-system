package com.restaurant.ordering.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    private Long waiterId;

    @NotEmpty(message = "The order must contain at least one item")
    private List<@Valid OrderItemRequest> items;
}
