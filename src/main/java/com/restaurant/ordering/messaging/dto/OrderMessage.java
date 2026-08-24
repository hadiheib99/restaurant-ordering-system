package com.restaurant.ordering.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Serializable JMS payload describing an order event sent to the kitchen queue.
 *
 * <p>The message contains only the information required by message consumers and
 * is serialized to JSON text by the configured JMS message converter.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
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
