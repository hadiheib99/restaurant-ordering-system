package com.restaurant.ordering.repository;

import com.restaurant.ordering.model.OrderStatus;
import com.restaurant.ordering.model.RestaurantOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data repository for {@link RestaurantOrder} persistence and role-oriented queries.
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
public interface OrderRepository extends JpaRepository<RestaurantOrder, Long> {

    /**
     * Retrieves orders that currently have one lifecycle status.
     *
     * @param status lifecycle status used for filtering
     * @return orders currently in the supplied status
     */
    List<RestaurantOrder> findByStatus(OrderStatus status);

    /**
     * Retrieves all orders owned by one customer.
     *
     * @param customerId customer identifier
     * @return orders owned by the specified customer
     */
    List<RestaurantOrder> findByCustomerId(Long customerId);

    /**
     * Retrieves all orders assigned to one waiter.
     *
     * @param waiterId waiter identifier
     * @return orders assigned to the specified waiter
     */
    List<RestaurantOrder> findByWaiterId(Long waiterId);

    /**
     * Retrieves orders belonging to the customer with the supplied email address.
     *
     * @param email customer email address
     * @return orders owned by the authenticated customer
     */
    List<RestaurantOrder> findByCustomerEmail(String email);
}
