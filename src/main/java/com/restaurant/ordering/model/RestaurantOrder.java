package com.restaurant.ordering.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.OptimisticLockType;
import org.hibernate.annotations.OptimisticLocking;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA aggregate representing a complete restaurant order.
 *
 * <p>An order belongs to a customer, may be assigned to a waiter, contains one
 * or more {@link OrderItem} records and moves through the {@link OrderStatus}
 * workflow. Creation/update timestamps are maintained automatically.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@Entity
@Table(name = "orders")
@DynamicUpdate
@OptimisticLocking(type = OptimisticLockType.ALL)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Customer who owns the order. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private User customer;

    /** Waiter assigned to the order, when applicable. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "waiter_id")
    private User waiter;

    /** Current lifecycle state of the order. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    /** Sum of all order-item subtotals. */
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** Line items that compose the order. */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    /** Initializes timestamps and default values before the first database insert. */
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) status = OrderStatus.NEW;
        if (totalPrice == null) totalPrice = BigDecimal.ZERO;
    }

    /** Refreshes the modification timestamp before an update statement. */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Adds an item while maintaining the bidirectional JPA relationship.
     * @param item item to attach to the order
     */
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    /**
     * Removes an item while maintaining the bidirectional JPA relationship.
     * @param item item to remove from the order
     */
    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }
}
