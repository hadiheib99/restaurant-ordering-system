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
    /** @param status lifecycle status @return orders currently in that status */
    List<RestaurantOrder> findByStatus(OrderStatus status);
    /** @param customerId customer identifier @return orders owned by that customer */
    List<RestaurantOrder> findByCustomerId(Long customerId);
    /** @param waiterId waiter identifier @return orders assigned to that waiter */
    List<RestaurantOrder> findByWaiterId(Long waiterId);
    /** @param email customer email @return orders owned by that authenticated customer */
    List<RestaurantOrder> findByCustomerEmail(String email);
}
