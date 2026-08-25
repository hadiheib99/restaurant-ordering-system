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

    /**
     * Retrieves the orders visible to the currently authenticated user.
     *
     * @return orders visible to the current user
     */
    List<OrderResponse> getAllOrders();

    /**
     * Retrieves one order by its unique identifier.
     *
     * @param id unique order identifier
     * @return the requested order
     */
    OrderResponse getOrderById(Long id);

    /**
     * Retrieves orders that currently match one lifecycle status.
     *
     * @param status status used to filter orders
     * @return orders that currently match the supplied status
     */
    List<OrderResponse> getOrdersByStatus(OrderStatus status);

    /**
     * Retrieves the complete order history for one customer.
     *
     * @param customerId unique customer identifier
     * @return all orders belonging to the customer
     */
    List<OrderResponse> getOrdersByCustomer(Long customerId);

    /**
     * Retrieves all orders assigned to one waiter.
     *
     * @param waiterId unique waiter identifier
     * @return all orders assigned to the waiter
     */
    List<OrderResponse> getOrdersByWaiter(Long waiterId);

    /**
     * Creates and persists a new restaurant order.
     *
     * @param request information required to create the order
     * @return the newly created order
     */
    OrderResponse createOrder(OrderRequest request);

    /**
     * Changes an order to a permitted next lifecycle status.
     *
     * @param id unique order identifier
     * @param status requested new status
     * @return the updated order
     */
    OrderResponse updateStatus(Long id, OrderStatus status);

    /**
     * Exports one order as an XML receipt.
     *
     * @param id unique order identifier
     * @return XML representation of the order receipt
     */
    String exportReceiptXml(Long id);

    /**
     * Exports an administrative XML report for restaurant orders.
     *
     * @return administrative restaurant report encoded as XML
     */
    String exportReportXml();

    /**
     * Permanently deletes an order by its identifier.
     *
     * @param id unique identifier of the order to delete
     */
    void deleteOrder(Long id);
}
