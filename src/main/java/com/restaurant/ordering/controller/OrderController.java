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

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable OrderStatus status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
    }

    @GetMapping("/waiter/{waiterId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByWaiter(@PathVariable Long waiterId) {
        return ResponseEntity.ok(orderService.getOrdersByWaiter(waiterId));
    }

    @GetMapping(value = "/{id}/receipt.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> exportReceiptXml(@PathVariable Long id) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=order-" + id + "-receipt.xml")
                .contentType(MediaType.APPLICATION_XML)
                .body(orderService.exportReceiptXml(id));
    }

    @GetMapping(value = "/report.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> exportReportXml() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=restaurant-orders-report.xml")
                .contentType(MediaType.APPLICATION_XML)
                .body(orderService.exportReportXml());
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus value
    ) {
        return ResponseEntity.ok(orderService.updateStatus(id, value));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
