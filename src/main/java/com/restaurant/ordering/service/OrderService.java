package com.restaurant.ordering.service;

import com.restaurant.ordering.dto.OrderRequest;
import com.restaurant.ordering.dto.OrderResponse;
import com.restaurant.ordering.model.OrderStatus;

import java.util.List;

/**
 * Defines the business operations available for restaurant orders.
 *
 * <p>The service API covers order retrieval, creation, status management,
 * XML export and deletion. Concrete implementations are responsible for
 * enforcing role-based permissions and valid order-state transitions.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
public interface OrderService {

    /** @return orders visible to the current authenticated user */
    List<OrderResponse> getAllOrders();

    /**
     * @param id unique order identifier
     * @return the requested order
     */
    OrderResponse getOrderById(Long id);

    /**
     * @param status status used to filter orders
     * @return orders that currently match the supplied status
     */
    List<OrderResponse> getOrdersByStatus(OrderStatus status);

    /**
     * @param customerId unique customer identifier
     * @return all orders belonging to the customer
     */
    List<OrderResponse> getOrdersByCustomer(Long customerId);

    /**
     * @param waiterId unique waiter identifier
     * @return all orders assigned to the waiter
     */
    List<OrderResponse> getOrdersByWaiter(Long waiterId);

    /**
     * @param request information required to create the order
     * @return the newly created order
     */
    OrderResponse createOrder(OrderRequest request);

    /**
     * @param id unique order identifier
     * @param status requested new status
     * @return the updated order
     */
    OrderResponse updateStatus(Long id, OrderStatus status);

    /**
     * @param id unique order identifier
     * @return XML representation of the order receipt
     */
    String exportReceiptXml(Long id);

    /** @return administrative restaurant report encoded as XML */
    String exportReportXml();

    /** @param id unique identifier of the order to delete */
    void deleteOrder(Long id);
}
