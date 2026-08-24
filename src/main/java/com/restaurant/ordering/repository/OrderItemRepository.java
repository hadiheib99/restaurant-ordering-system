package com.restaurant.ordering.repository;

import com.restaurant.ordering.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository providing CRUD persistence for {@link OrderItem} rows.
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
