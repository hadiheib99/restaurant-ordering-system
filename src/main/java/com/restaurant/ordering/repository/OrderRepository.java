package com.restaurant.ordering.repository;

import com.restaurant.ordering.model.OrderStatus;
import com.restaurant.ordering.model.RestaurantOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<RestaurantOrder, Long> {

    List<RestaurantOrder> findByStatus(OrderStatus status);

    List<RestaurantOrder> findByCustomerId(Long customerId);

    List<RestaurantOrder> findByWaiterId(Long waiterId);

    List<RestaurantOrder> findByCustomerEmail(String email);

}