package com.restaurant.ordering.controller;

import com.restaurant.ordering.dto.OrderRequest;
import com.restaurant.ordering.dto.OrderResponse;
import com.restaurant.ordering.model.OrderStatus;
import com.restaurant.ordering.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller that exposes HTTP endpoints for restaurant order operations.
 *
 * <p>The controller delegates business rules to {@link OrderService}. It provides
 * endpoints for creating orders, reading orders, changing order status,
 * exporting XML receipts/reports and deleting orders. Authorization rules are
 * enforced by the security layer and the service layer.</p>
 *
 * <p>Base API path: {@code /api/orders}</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Returns all orders visible to the currently authenticated user.
     * Staff members may receive all orders, while customers receive only their
     * own orders according to the service authorization rules.
     *
     * @return HTTP 200 response containing the visible orders
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    /**
     * Retrieves one order by its unique identifier.
     *
     * @param id unique identifier of the order
     * @return HTTP 200 response containing the requested order
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    /**
     * Returns orders that currently have the requested status.
     * This endpoint is intended for restaurant staff.
     *
     * @param status order status used as a filter
     * @return HTTP 200 response containing matching orders
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable OrderStatus status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    /**
     * Returns all orders associated with a specific customer.
     *
     * @param customerId unique identifier of the customer
     * @return HTTP 200 response containing the customer's orders
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
    }

    /**
     * Returns all orders assigned to a specific waiter.
     *
     * @param waiterId unique identifier of the waiter
     * @return HTTP 200 response containing orders assigned to the waiter
     */
    @GetMapping("/waiter/{waiterId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByWaiter(@PathVariable Long waiterId) {
        return ResponseEntity.ok(orderService.getOrdersByWaiter(waiterId));
    }

    /**
     * Exports a single order receipt as an XML document.
     *
     * @param id unique identifier of the order
     * @return XML receipt returned as a downloadable attachment
     */
    @GetMapping(value = "/{id}/receipt.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> exportReceiptXml(@PathVariable Long id) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=order-" + id + "-receipt.xml")
                .contentType(MediaType.APPLICATION_XML)
                .body(orderService.exportReceiptXml(id));
    }

    /**
     * Exports the administrative restaurant order report as XML.
     * The service layer restricts this operation to administrators.
     *
     * @return XML report returned as a downloadable attachment
     */
    @GetMapping(value = "/report.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> exportReportXml() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=restaurant-orders-report.xml")
                .contentType(MediaType.APPLICATION_XML)
                .body(orderService.exportReportXml());
    }

    /**
     * Creates a new restaurant order from a validated request.
     *
     * @param request customer, optional waiter and ordered meal information
     * @return HTTP 201 response containing the newly created order
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request));
    }

    /**
     * Changes the status of an existing order.
     * Allowed transitions depend on the current order status and user role.
     *
     * @param id unique identifier of the order
     * @param value new requested order status
     * @return HTTP 200 response containing the updated order
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus value
    ) {
        return ResponseEntity.ok(orderService.updateStatus(id, value));
    }

    /**
     * Permanently deletes an order.
     * This operation is intended for authorized administrative users.
     *
     * @param id unique identifier of the order to delete
     * @return HTTP 204 response when the order was deleted successfully
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
