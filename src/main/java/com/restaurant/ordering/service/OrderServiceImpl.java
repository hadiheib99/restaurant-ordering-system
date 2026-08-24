package com.restaurant.ordering.service;

import com.restaurant.ordering.dto.*;
import com.restaurant.ordering.exception.ResourceNotFoundException;
import com.restaurant.ordering.messaging.dto.OrderMessage;
import com.restaurant.ordering.messaging.producer.OrderProducer;
import com.restaurant.ordering.model.*;
import com.restaurant.ordering.repository.MealRepository;
import com.restaurant.ordering.repository.OrderRepository;
import com.restaurant.ordering.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Main business-service implementation for restaurant orders.
 *
 * <p>This service coordinates order persistence, customer ownership rules,
 * role-specific status transitions, price calculations, JMS kitchen events and
 * XML receipt/report export. Authorization is checked both by Spring Security
 * and by object-level rules in this class.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final MealRepository mealRepository;
    private final OrderProducer orderProducer;

    /**
     * {@inheritDoc}
     * <p>Staff receive all orders; customers receive only orders owned by their authenticated email.</p>
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        if (isStaff()) {
            return orderRepository.findAll().stream().map(this::toResponse).toList();
        }
        return orderRepository.findByCustomerEmail(getCurrentUserEmail()).stream().map(this::toResponse).toList();
    }

    /**
     * {@inheritDoc}
     * @throws ResourceNotFoundException when the order does not exist
     * @throws AccessDeniedException when a customer attempts to view another customer's order
     */
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        RestaurantOrder order = findOrder(id);
        ensureCanView(order);
        return toResponse(order);
    }

    /**
     * {@inheritDoc}
     * @throws AccessDeniedException when the current user is not restaurant staff
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        ensureStaff();
        return orderRepository.findByStatus(status).stream().map(this::toResponse).toList();
    }

    /**
     * {@inheritDoc}
     * <p>Customers may request only their own order history; staff may request any customer.</p>
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByCustomer(Long customerId) {
        if (!isStaff()) {
            User customer = findUser(customerId);
            if (!customer.getEmail().equalsIgnoreCase(getCurrentUserEmail())) {
                throw new AccessDeniedException("You may only view your own orders");
            }
        }
        return orderRepository.findByCustomerId(customerId).stream().map(this::toResponse).toList();
    }

    /**
     * {@inheritDoc}
     * @throws AccessDeniedException when the current user is not restaurant staff
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByWaiter(Long waiterId) {
        ensureStaff();
        return orderRepository.findByWaiterId(waiterId).stream().map(this::toResponse).toList();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Validates customer/waiter roles, verifies meal availability, captures the
     * current unit price, calculates every subtotal and total, persists the complete
     * aggregate and publishes a NEW JMS event.</p>
     *
     * @throws ResourceNotFoundException when a referenced user or meal does not exist
     * @throws IllegalArgumentException when roles or meal availability are invalid
     * @throws AccessDeniedException when a customer creates an order for another customer
     */
    @Override
    public OrderResponse createOrder(OrderRequest request) {
        User customer = findUser(request.getCustomerId());
        if (customer.getRole() != Role.CUSTOMER) {
            throw new IllegalArgumentException("The selected user must have the CUSTOMER role");
        }
        if (hasRole("ROLE_CUSTOMER") && !customer.getEmail().equalsIgnoreCase(getCurrentUserEmail())) {
            throw new AccessDeniedException("Customers may only create orders for themselves");
        }

        User waiter = null;
        if (request.getWaiterId() != null) {
            waiter = findUser(request.getWaiterId());
            if (waiter.getRole() != Role.WAITER) {
                throw new IllegalArgumentException("The selected waiter must have the WAITER role");
            }
        }

        RestaurantOrder order = RestaurantOrder.builder()
                .customer(customer)
                .waiter(waiter)
                .status(OrderStatus.NEW)
                .totalPrice(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.getItems()) {
            Meal meal = mealRepository.findById(itemRequest.getMealId())
                    .orElseThrow(() -> new ResourceNotFoundException("Meal not found with ID: " + itemRequest.getMealId()));
            if (!Boolean.TRUE.equals(meal.getAvailable())) {
                throw new IllegalArgumentException("Meal is not available: " + meal.getName());
            }
            OrderItem item = OrderItem.builder()
                    .meal(meal)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(meal.getPrice())
                    .build();
            item.calculateSubtotal();
            order.addItem(item);
            total = total.add(item.getSubtotal());
        }

        order.setTotalPrice(total);
        RestaurantOrder savedOrder = orderRepository.save(order);
        sendOrderEvent(savedOrder);
        return toResponse(savedOrder);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Both the workflow transition and the authenticated role are validated
     * before persistence. Successful changes publish a JMS status event.</p>
     *
     * @throws IllegalArgumentException when the requested workflow transition is invalid
     * @throws AccessDeniedException when the current role is not allowed to perform the transition
     */
    @Override
    public OrderResponse updateStatus(Long id, OrderStatus newStatus) {
        RestaurantOrder order = findOrder(id);
        validateStatusTransition(order.getStatus(), newStatus);
        validateStatusPermission(order, newStatus);
        order.setStatus(newStatus);
        RestaurantOrder savedOrder = orderRepository.save(order);
        sendOrderEvent(savedOrder);
        return toResponse(savedOrder);
    }

    /**
     * {@inheritDoc}
     * @throws AccessDeniedException when a customer requests another customer's receipt
     */
    @Override
    @Transactional(readOnly = true)
    public String exportReceiptXml(Long id) {
        RestaurantOrder order = findOrder(id);
        ensureCanView(order);
        return receiptXml(order);
    }

    /**
     * {@inheritDoc}
     * <p>The report includes total orders, paid revenue, status counts and a compact order list.</p>
     * @throws AccessDeniedException when the current user is not an administrator
     */
    @Override
    @Transactional(readOnly = true)
    public String exportReportXml() {
        if (!hasRole("ROLE_ADMIN")) {
            throw new AccessDeniedException("Only administrators may export restaurant reports");
        }

        List<RestaurantOrder> orders = orderRepository.findAll();
        BigDecimal paidRevenue = orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.PAID)
                .map(RestaurantOrder::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<restaurantReport generatedAt=\"").append(xmlEscape(OffsetDateTime.now().toString())).append("\">\n");
        xml.append("  <summary>\n");
        xml.append("    <totalOrders>").append(orders.size()).append("</totalOrders>\n");
        xml.append("    <paidRevenue>").append(paidRevenue).append("</paidRevenue>\n");
        for (OrderStatus status : OrderStatus.values()) {
            long count = orders.stream().filter(order -> order.getStatus() == status).count();
            xml.append("    <status name=\"").append(status).append("\" count=\"").append(count).append("\"/>\n");
        }
        xml.append("  </summary>\n  <orders>\n");
        for (RestaurantOrder order : orders) {
            xml.append("    <order id=\"").append(order.getId()).append("\" status=\"").append(order.getStatus()).append("\">\n");
            xml.append("      <customer>").append(xmlEscape(getFullName(order.getCustomer()))).append("</customer>\n");
            xml.append("      <totalPrice>").append(order.getTotalPrice()).append("</totalPrice>\n");
            xml.append("      <createdAt>").append(xmlEscape(String.valueOf(order.getCreatedAt()))).append("</createdAt>\n");
            xml.append("    </order>\n");
        }
        xml.append("  </orders>\n</restaurantReport>\n");
        return xml.toString();
    }

    /** {@inheritDoc} */
    @Override
    public void deleteOrder(Long id) {
        RestaurantOrder order = findOrder(id);
        orderRepository.delete(order);
    }

    /** Loads an order entity or throws a domain-specific not-found exception. */
    private RestaurantOrder findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
    }

    /** Loads a user entity or throws a domain-specific not-found exception. */
    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    /** Ensures customers can only access their own orders while staff may access all orders. */
    private void ensureCanView(RestaurantOrder order) {
        if (isStaff()) return;
        if (!order.getCustomer().getEmail().equalsIgnoreCase(getCurrentUserEmail())) {
            throw new AccessDeniedException("You may only view your own orders");
        }
    }

    /** Ensures the current authenticated user has a restaurant-staff role. */
    private void ensureStaff() {
        if (!isStaff()) throw new AccessDeniedException("This operation is only available to restaurant staff");
    }

    /** @return true for ADMIN, WAITER or CHEF roles */
    private boolean isStaff() {
        return hasRole("ROLE_ADMIN") || hasRole("ROLE_WAITER") || hasRole("ROLE_CHEF");
    }

    /** Checks whether the current authentication contains one specific authority. */
    private boolean hasRole(String role) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }

    /** @return email/subject of the currently authenticated JWT principal */
    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    /** Builds and publishes a compact JMS event for an order. */
    private void sendOrderEvent(RestaurantOrder order) {
        OrderMessage message = new OrderMessage(
                order.getId(), order.getCustomer().getId(), getFullName(order.getCustomer()),
                order.getTotalPrice(), order.getStatus().name()
        );
        orderProducer.sendOrderEvent(message);
    }

    /** Converts an order aggregate and line items into a REST response DTO. */
    private OrderResponse toResponse(RestaurantOrder order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .mealId(item.getMeal().getId())
                        .mealName(item.getMeal().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomer().getId())
                .customerName(getFullName(order.getCustomer()))
                .waiterId(order.getWaiter() != null ? order.getWaiter().getId() : null)
                .waiterName(order.getWaiter() != null ? getFullName(order.getWaiter()) : null)
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(itemResponses)
                .build();
    }

    /** @return first and last name combined for display and XML output */
    private String getFullName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }

    /** Validates that a requested order-status move follows the defined lifecycle. */
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == newStatus) return;
        boolean validTransition = switch (currentStatus) {
            case NEW -> newStatus == OrderStatus.PREPARING || newStatus == OrderStatus.CANCELLED;
            case PREPARING -> newStatus == OrderStatus.READY || newStatus == OrderStatus.CANCELLED;
            case READY -> newStatus == OrderStatus.SERVED;
            case SERVED -> newStatus == OrderStatus.PAID;
            case PAID, CANCELLED -> false;
        };
        if (!validTransition) {
            throw new IllegalArgumentException("Invalid order status transition from " + currentStatus + " to " + newStatus);
        }
    }

    /** Applies role/ownership rules to an otherwise valid status transition. */
    private void validateStatusPermission(RestaurantOrder order, OrderStatus newStatus) {
        OrderStatus currentStatus = order.getStatus();
        if (hasRole("ROLE_ADMIN")) return;

        boolean cancellingBeforeReady = newStatus == OrderStatus.CANCELLED &&
                (currentStatus == OrderStatus.NEW || currentStatus == OrderStatus.PREPARING);

        if (hasRole("ROLE_CUSTOMER")) {
            boolean ownOrder = order.getCustomer().getEmail().equalsIgnoreCase(getCurrentUserEmail());
            if (cancellingBeforeReady && ownOrder) return;
        }

        if (hasRole("ROLE_WAITER")) {
            if (cancellingBeforeReady ||
                    (currentStatus == OrderStatus.READY && newStatus == OrderStatus.SERVED) ||
                    (currentStatus == OrderStatus.SERVED && newStatus == OrderStatus.PAID)) {
                return;
            }
        }

        if (hasRole("ROLE_CHEF")) {
            if ((currentStatus == OrderStatus.NEW && newStatus == OrderStatus.PREPARING) ||
                    (currentStatus == OrderStatus.PREPARING && newStatus == OrderStatus.READY)) {
                return;
            }
        }

        throw new AccessDeniedException(
                "Your role is not allowed to change order status from " + currentStatus + " to " + newStatus
        );
    }

    /** Serializes a complete order as a simple XML receipt. */
    private String receiptXml(RestaurantOrder order) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<receipt orderId=\"").append(order.getId()).append("\">\n");
        xml.append("  <status>").append(order.getStatus()).append("</status>\n");
        xml.append("  <customer>").append(xmlEscape(getFullName(order.getCustomer()))).append("</customer>\n");
        xml.append("  <createdAt>").append(xmlEscape(String.valueOf(order.getCreatedAt()))).append("</createdAt>\n");
        xml.append("  <items>\n");
        for (OrderItem item : order.getItems()) {
            xml.append("    <item>\n");
            xml.append("      <meal>").append(xmlEscape(item.getMeal().getName())).append("</meal>\n");
            xml.append("      <quantity>").append(item.getQuantity()).append("</quantity>\n");
            xml.append("      <unitPrice>").append(item.getUnitPrice()).append("</unitPrice>\n");
            xml.append("      <subtotal>").append(item.getSubtotal()).append("</subtotal>\n");
            xml.append("    </item>\n");
        }
        xml.append("  </items>\n");
        xml.append("  <totalPrice>").append(order.getTotalPrice()).append("</totalPrice>\n");
        xml.append("</receipt>\n");
        return xml.toString();
    }

    /** Escapes XML-sensitive characters before untrusted text is inserted into generated XML. */
    private String xmlEscape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
