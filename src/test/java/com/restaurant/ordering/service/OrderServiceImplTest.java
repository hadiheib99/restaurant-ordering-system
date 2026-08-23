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

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private MealRepository mealRepository;
    @Mock private OrderProducer orderProducer;

    private OrderServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OrderServiceImpl(orderRepository, userRepository, mealRepository, orderProducer);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

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

    @Test
    void chefCannotServeReadyOrder() {
        authenticate("chef@restaurant.com", "ROLE_CHEF");
        RestaurantOrder order = order(OrderStatus.READY);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        assertThrows(AccessDeniedException.class, () -> service.updateStatus(1L, OrderStatus.SERVED));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void waiterCanMoveReadyOrderToServed() {
        authenticate("waiter@restaurant.com", "ROLE_WAITER");
        RestaurantOrder order = order(OrderStatus.READY);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        assertEquals(OrderStatus.SERVED, service.updateStatus(1L, OrderStatus.SERVED).getStatus());
    }

    @Test
    void waiterCanCancelBeforeReady() {
        authenticate("waiter@restaurant.com", "ROLE_WAITER");
        RestaurantOrder order = order(OrderStatus.PREPARING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        assertEquals(OrderStatus.CANCELLED, service.updateStatus(1L, OrderStatus.CANCELLED).getStatus());
    }

    @Test
    void customerCanCancelOwnOrderBeforeReady() {
        authenticate("customer@example.com", "ROLE_CUSTOMER");
        RestaurantOrder order = order(OrderStatus.NEW);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        assertEquals(OrderStatus.CANCELLED, service.updateStatus(1L, OrderStatus.CANCELLED).getStatus());
    }

    @Test
    void customerCannotCancelAnotherCustomersOrder() {
        authenticate("other@example.com", "ROLE_CUSTOMER");
        RestaurantOrder order = order(OrderStatus.NEW);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        assertThrows(AccessDeniedException.class, () -> service.updateStatus(1L, OrderStatus.CANCELLED));
    }

    @Test
    void orderCannotBeCancelledOnceReady() {
        authenticate("waiter@restaurant.com", "ROLE_WAITER");
        RestaurantOrder order = order(OrderStatus.READY);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        assertThrows(IllegalArgumentException.class, () -> service.updateStatus(1L, OrderStatus.CANCELLED));
    }

    @Test
    void waiterCannotStartPreparingNewOrder() {
        authenticate("waiter@restaurant.com", "ROLE_WAITER");
        RestaurantOrder order = order(OrderStatus.NEW);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        assertThrows(AccessDeniedException.class, () -> service.updateStatus(1L, OrderStatus.PREPARING));
    }

    @Test
    void adminCanPerformAnyValidTransition() {
        authenticate("admin@restaurant.com", "ROLE_ADMIN");
        RestaurantOrder order = order(OrderStatus.SERVED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        assertEquals(OrderStatus.PAID, service.updateStatus(1L, OrderStatus.PAID).getStatus());
    }

    @Test
    void invalidTransitionIsRejectedEvenForAdmin() {
        authenticate("admin@restaurant.com", "ROLE_ADMIN");
        RestaurantOrder order = order(OrderStatus.NEW);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        assertThrows(IllegalArgumentException.class, () -> service.updateStatus(1L, OrderStatus.PAID));
    }

    @Test
    void customerOnlyReceivesOwnOrders() {
        authenticate("customer@example.com", "ROLE_CUSTOMER");
        RestaurantOrder ownOrder = order(OrderStatus.NEW);
        when(orderRepository.findByCustomerEmail("customer@example.com")).thenReturn(List.of(ownOrder));
        var result = service.getAllOrders();
        assertEquals(1, result.size());
        verify(orderRepository, never()).findAll();
    }

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

    @Test
    void adminCanExportXmlReport() {
        authenticate("admin@restaurant.com", "ROLE_ADMIN");
        when(orderRepository.findAll()).thenReturn(List.of(order(OrderStatus.PAID), order(OrderStatus.NEW)));
        String xml = service.exportReportXml();
        assertTrue(xml.contains("<restaurantReport"));
        assertTrue(xml.contains("<totalOrders>2</totalOrders>"));
        assertTrue(xml.contains("<paidRevenue>40.00</paidRevenue>"));
    }

    @Test
    void nonAdminCannotExportXmlReport() {
        authenticate("waiter@restaurant.com", "ROLE_WAITER");
        assertThrows(AccessDeniedException.class, service::exportReportXml);
    }

    private static void authenticate(String email, String role) {
        var authentication = new UsernamePasswordAuthenticationToken(
                email, "n/a", List.of(new SimpleGrantedAuthority(role))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

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
