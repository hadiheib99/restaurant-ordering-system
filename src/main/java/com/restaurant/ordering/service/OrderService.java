package com.restaurant.ordering.service;

import com.restaurant.ordering.dto.OrderRequest;
import com.restaurant.ordering.dto.OrderResponse;
import com.restaurant.ordering.model.OrderStatus;

import java.util.List;

public interface OrderService {

    List<OrderResponse> getAllOrders();

    OrderResponse getOrderById(Long id);

    List<OrderResponse> getOrdersByStatus(OrderStatus status);

    List<OrderResponse> getOrdersByCustomer(Long customerId);

    List<OrderResponse> getOrdersByWaiter(Long waiterId);

    OrderResponse createOrder(OrderRequest request);

    OrderResponse updateStatus(Long id, OrderStatus status);

    void deleteOrder(Long id);
}