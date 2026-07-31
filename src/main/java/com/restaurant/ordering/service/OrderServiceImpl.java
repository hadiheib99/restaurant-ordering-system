package com.restaurant.ordering.service;

import com.restaurant.ordering.dto.*;
import com.restaurant.ordering.exception.ResourceNotFoundException;
import com.restaurant.ordering.model.*;
import com.restaurant.ordering.repository.MealRepository;
import com.restaurant.ordering.repository.OrderRepository;
import com.restaurant.ordering.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.restaurant.ordering.messaging.producer.OrderProducer;
import com.restaurant.ordering.messaging.dto.OrderMessage;
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
        return orderRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        return toResponse(findOrder(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByWaiter(Long waiterId) {
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

            if (!Boolean.TRUE.equals(meal.getAvailable())){
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
        OrderMessage message = new OrderMessage(
                savedOrder.getId(),
                savedOrder.getCustomer().getId(),
                savedOrder.getCustomer().getFirstName() + " " +
                        savedOrder.getCustomer().getLastName(),
                savedOrder.getTotalPrice(),
                savedOrder.getStatus().name()
        );

        orderProducer.sendOrder(message);
        return toResponse(savedOrder);
    }

    @Override
    public OrderResponse updateStatus(Long id, OrderStatus status) {
        RestaurantOrder order = findOrder(id);
        order.setStatus(status);

        return toResponse(orderRepository.save(order));
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
                .waiterId(
                        order.getWaiter() != null
                                ? order.getWaiter().getId()
                                : null
                )
                .waiterName(
                        order.getWaiter() != null
                                ? getFullName(order.getWaiter())
                                : null
                )
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
}