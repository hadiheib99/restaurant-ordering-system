package com.restaurant.ordering.service;

import com.restaurant.ordering.messaging.producer.OrderProducer;
import com.restaurant.ordering.model.OrderStatus;
import com.restaurant.ordering.model.RestaurantOrder;
import com.restaurant.ordering.model.Role;
import com.restaurant.ordering.model.User;
import com.restaurant.ordering.repository.MealRepository;
import com.restaurant.ordering.repository.OrderRepository;
import com.restaurant.ordering.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link OrderServiceImpl} order workflow, authorization, messaging and XML export behavior.
 *
 * <p>The suite verifies role-specific status transitions for chefs, waiters, customers
 * and administrators, customer ownership restrictions, cancellation rules, JMS event
 * publication and XML receipt/report generation and authorization.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private MealRepository mealRepository;
    @Mock private OrderProducer orderProducer;

    private OrderServiceImpl service;

    /** Creates a fresh order service with mocked repositories and JMS producer before each test. */
    @BeforeEach
    void setUp() {
        service = new OrderServiceImpl(orderRepository, userRepository, mealRepository, orderProducer);
    }

    /** Clears the Spring Security context after each test to prevent role leakage between tests. */
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    /** Verifies that a chef may move a NEW order to PREPARING and that a JMS event is published. */
    @Test
    void chefCanMoveNewOrderToPreparing() {
        authenticate("chef@restaurant.com", "ROLE_CHEF");
        RestaurantOrder order = order(OrderStatus.NEW);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        var response = service.updateStatus(1L, OrderStatus.PREPARING);
        assertEquals(OrderStatus.PREPARING, response.getStatus());
        verify(orderProducer).sendOrderEvent(any());
    }

    /** Verifies that a chef cannot perform the waiter's READY-to-SERVED transition. */
    @Test
    void chefCannotServeReadyOrder() {
        authenticate("chef@restaurant.com", "ROLE_CHEF");
        RestaurantOrder order = order(OrderStatus.READY);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        assertThrows(AccessDeniedException.class, () -> service.updateStatus(1L, OrderStatus.SERVED));
        verify(orderRepository, never()).save(any());
    }

    /** Verifies that a waiter may move a READY order to SERVED. */
    @Test
    void waiterCanMoveReadyOrderToServed() {
        authenticate("waiter@restaurant.com", "ROLE_WAITER");
        RestaurantOrder order = order(OrderStatus.READY);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        assertEquals(OrderStatus.SERVED, service.updateStatus(1L, OrderStatus.SERVED).getStatus());
    }

    /** Verifies that a waiter may cancel an order while it is still before READY. */
    @Test
    void waiterCanCancelBeforeReady() {
        authenticate("waiter@restaurant.com", "ROLE_WAITER");
        RestaurantOrder order = order(OrderStatus.PREPARING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        assertEquals(OrderStatus.CANCELLED, service.updateStatus(1L, OrderStatus.CANCELLED).getStatus());
    }

    /** Verifies that a customer may cancel their own order before it reaches READY. */
    @Test
    void customerCanCancelOwnOrderBeforeReady() {
        authenticate("customer@example.com", "ROLE_CUSTOMER");
        RestaurantOrder order = order(OrderStatus.NEW);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        assertEquals(OrderStatus.CANCELLED, service.updateStatus(1L, OrderStatus.CANCELLED).getStatus());
    }

    /** Verifies that a customer cannot cancel another customer's order. */
    @Test
    void customerCannotCancelAnotherCustomersOrder() {
        authenticate("other@example.com", "ROLE_CUSTOMER");
        RestaurantOrder order = order(OrderStatus.NEW);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        assertThrows(AccessDeniedException.class, () -> service.updateStatus(1L, OrderStatus.CANCELLED));
    }

    /** Verifies that cancellation is rejected after an order has reached READY. */
    @Test
    void orderCannotBeCancelledOnceReady() {
        authenticate("waiter@restaurant.com", "ROLE_WAITER");
        RestaurantOrder order = order(OrderStatus.READY);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        assertThrows(IllegalArgumentException.class, () -> service.updateStatus(1L, OrderStatus.CANCELLED));
    }

    /** Verifies that a waiter cannot perform the chef's NEW-to-PREPARING transition. */
    @Test
    void waiterCannotStartPreparingNewOrder() {
        authenticate("waiter@restaurant.com", "ROLE_WAITER");
        RestaurantOrder order = order(OrderStatus.NEW);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        assertThrows(AccessDeniedException.class, () -> service.updateStatus(1L, OrderStatus.PREPARING));
    }

    /** Verifies that an administrator may perform any transition that is valid in the order lifecycle. */
    @Test
    void adminCanPerformAnyValidTransition() {
        authenticate("admin@restaurant.com", "ROLE_ADMIN");
        RestaurantOrder order = order(OrderStatus.SERVED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        assertEquals(OrderStatus.PAID, service.updateStatus(1L, OrderStatus.PAID).getStatus());
    }

    /** Verifies that even an administrator cannot skip required lifecycle states. */
    @Test
    void invalidTransitionIsRejectedEvenForAdmin() {
        authenticate("admin@restaurant.com", "ROLE_ADMIN");
        RestaurantOrder order = order(OrderStatus.NEW);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        assertThrows(IllegalArgumentException.class, () -> service.updateStatus(1L, OrderStatus.PAID));
    }

    /** Verifies that a customer sees only orders belonging to the authenticated email address. */
    @Test
    void customerOnlyReceivesOwnOrders() {
        authenticate("customer@example.com", "ROLE_CUSTOMER");
        RestaurantOrder ownOrder = order(OrderStatus.NEW);
        when(orderRepository.findByCustomerEmail("customer@example.com")).thenReturn(List.of(ownOrder));
        var result = service.getAllOrders();
        assertEquals(1, result.size());
        verify(orderRepository, never()).findAll();
    }

    /** Verifies that a customer can export their own order receipt as correctly structured XML. */
    @Test
    void customerCanExportOwnReceiptAsXml() {
        authenticate("customer@example.com", "ROLE_CUSTOMER");
        RestaurantOrder order = order(OrderStatus.NEW);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        String xml = service.exportReceiptXml(1L);
        assertTrue(xml.contains("<receipt orderId=\"1\">"));
        assertTrue(xml.contains("<customer>John Smith</customer>"));
        assertTrue(xml.contains("<totalPrice>40.00</totalPrice>"));
    }

    /** Verifies that an administrator can export an XML report with totals and paid revenue. */
    @Test
    void adminCanExportXmlReport() {
        authenticate("admin@restaurant.com", "ROLE_ADMIN");
        when(orderRepository.findAll()).thenReturn(List.of(order(OrderStatus.PAID), order(OrderStatus.NEW)));
        String xml = service.exportReportXml();
        assertTrue(xml.contains("<restaurantReport"));
        assertTrue(xml.contains("<totalOrders>2</totalOrders>"));
        assertTrue(xml.contains("<paidRevenue>40.00</paidRevenue>"));
    }

    /** Verifies that the complete restaurant XML report is restricted to administrators. */
    @Test
    void nonAdminCannotExportXmlReport() {
        authenticate("waiter@restaurant.com", "ROLE_WAITER");
        assertThrows(AccessDeniedException.class, service::exportReportXml);
    }

    /**
     * Installs a minimal authenticated principal in the Spring Security context.
     *
     * @param email principal email used as the authentication name
     * @param role Spring Security authority assigned to the principal
     */
    private static void authenticate(String email, String role) {
        var authentication = new UsernamePasswordAuthenticationToken(
                email, "n/a", List.of(new SimpleGrantedAuthority(role))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * Creates a compact order fixture owned by the standard test customer.
     *
     * @param status lifecycle status assigned to the order
     * @return restaurant order fixture with ID 1 and total price 40.00
     */
    private static RestaurantOrder order(OrderStatus status) {
        User customer = new User();
        customer.setFirstName("John");
        customer.setLastName("Smith");
        customer.setEmail("customer@example.com");
        customer.setRole(Role.CUSTOMER);

        return RestaurantOrder.builder()
                .id(1L)
                .customer(customer)
                .status(status)
                .totalPrice(new BigDecimal("40.00"))
                .items(List.of())
                .build();
    }
}
