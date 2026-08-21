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
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final MealRepository mealRepository;
    private final OrderProducer orderProducer;

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        if (isStaff()) {
            return orderRepository.findAll()
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        return orderRepository
                .findByCustomerEmail(getCurrentUserEmail())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        RestaurantOrder order = findOrder(id);
        ensureCanView(order);
        return toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        ensureStaff();
        return orderRepository.findByStatus(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByCustomer(Long customerId) {
        if (!isStaff()) {
            User customer = findUser(customerId);
            if (!customer.getEmail().equalsIgnoreCase(getCurrentUserEmail())) {
                throw new AccessDeniedException("You may only view your own orders");
            }
        }

        return orderRepository.findByCustomerId(customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByWaiter(Long waiterId) {
        ensureStaff();
        return orderRepository.findByWaiterId(waiterId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public OrderResponse createOrder(OrderRequest request) {
        User customer = findUser(request.getCustomerId());

        if (customer.getRole() != Role.CUSTOMER) {
            throw new IllegalArgumentException(
                    "The selected user must have the CUSTOMER role"
            );
        }

        if (hasRole("ROLE_CUSTOMER") &&
                !customer.getEmail().equalsIgnoreCase(getCurrentUserEmail())) {
            throw new AccessDeniedException("Customers may only create orders for themselves");
        }

        User waiter = null;

        if (request.getWaiterId() != null) {
            waiter = findUser(request.getWaiterId());

            if (waiter.getRole() != Role.WAITER) {
                throw new IllegalArgumentException(
                        "The selected waiter must have the WAITER role"
                );
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
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Meal not found with ID: " + itemRequest.getMealId()
                    ));

            if (!Boolean.TRUE.equals(meal.getAvailable())) {
                throw new IllegalArgumentException(
                        "Meal is not available: " + meal.getName()
                );
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

    @Override
    public OrderResponse updateStatus(Long id, OrderStatus newStatus) {
        RestaurantOrder order = findOrder(id);
        validateStatusTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);

        RestaurantOrder savedOrder = orderRepository.save(order);
        sendOrderEvent(savedOrder);

        return toResponse(savedOrder);
    }

    @Override
    public void deleteOrder(Long id) {
        RestaurantOrder order = findOrder(id);
        orderRepository.delete(order);
    }

    private RestaurantOrder findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with ID: " + id
                ));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID: " + id
                ));
    }

    private void ensureCanView(RestaurantOrder order) {
        if (isStaff()) {
            return;
        }

        if (!order.getCustomer().getEmail().equalsIgnoreCase(getCurrentUserEmail())) {
            throw new AccessDeniedException("You may only view your own orders");
        }
    }

    private void ensureStaff() {
        if (!isStaff()) {
            throw new AccessDeniedException("This operation is only available to restaurant staff");
        }
    }

    private boolean isStaff() {
        return hasRole("ROLE_ADMIN") ||
                hasRole("ROLE_WAITER") ||
                hasRole("ROLE_CHEF");
    }

    private boolean hasRole(String role) {
        return SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }

    private String getCurrentUserEmail() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    private void sendOrderEvent(RestaurantOrder order) {
        OrderMessage message = new OrderMessage(
                order.getId(),
                order.getCustomer().getId(),
                getFullName(order.getCustomer()),
                order.getTotalPrice(),
                order.getStatus().name()
        );
        orderProducer.sendOrderEvent(message);
    }

    private OrderResponse toResponse(RestaurantOrder order) {
        List<OrderItemResponse> itemResponses = order.getItems()
                .stream()
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

    private String getFullName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }

    private void validateStatusTransition(
            OrderStatus currentStatus,
            OrderStatus newStatus
    ) {
        if (currentStatus == newStatus) {
            return;
        }

        boolean validTransition = switch (currentStatus) {
            case NEW ->
                    newStatus == OrderStatus.PREPARING ||
                            newStatus == OrderStatus.CANCELLED;

            case PREPARING ->
                    newStatus == OrderStatus.READY ||
                            newStatus == OrderStatus.CANCELLED;

            case READY -> newStatus == OrderStatus.SERVED;
            case SERVED -> newStatus == OrderStatus.PAID;
            case PAID, CANCELLED -> false;
        };

        if (!validTransition) {
            throw new IllegalArgumentException(
                    "Invalid order status transition from "
                            + currentStatus
                            + " to "
                            + newStatus
            );
        }
    }
}
