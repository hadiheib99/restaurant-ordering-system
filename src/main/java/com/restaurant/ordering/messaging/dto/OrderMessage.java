package com.restaurant.ordering.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderMessage implements Serializable {

    private Long orderId;

    private Long customerId;

    private String customerName;

    private BigDecimal totalPrice;

    private String status;
}