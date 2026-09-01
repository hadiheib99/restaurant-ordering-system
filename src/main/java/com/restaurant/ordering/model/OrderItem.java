package com.restaurant.ordering.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.OptimisticLockType;
import org.hibernate.annotations.OptimisticLocking;

import java.math.BigDecimal;

/**
 * JPA entity representing one line item inside a restaurant order.
 *
 * <p>The entity stores the selected meal, ordered quantity, unit price captured
 * at ordering time and calculated subtotal. Capturing the unit price preserves
 * historical receipts even if the menu price changes later.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@Entity
@Table(name = "order_items")
@DynamicUpdate
@OptimisticLocking(type = OptimisticLockType.ALL)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Parent order that owns this item. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private RestaurantOrder order;

    /** Meal selected by the customer. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meal_id", nullable = false)
    private Meal meal;

    /** Number of meal units ordered. */
    @Column(nullable = false)
    private Integer quantity;

    /** Meal price captured at the moment the order is created. */
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /** Calculated value of {@code unitPrice * quantity}. */
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    /** Calculates and stores the monetary subtotal for this line item. */
    public void calculateSubtotal() {
        if (unitPrice == null || quantity == null) {
            subtotal = BigDecimal.ZERO;
            return;
        }
        subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    /** Recalculates the subtotal automatically before insert or update. */
    @PrePersist
    @PreUpdate
    public void updateSubtotal() {
        calculateSubtotal();
    }
}
