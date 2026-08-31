package com.restaurant.ordering.security.config;

import com.restaurant.ordering.controller.OrderController;
import com.restaurant.ordering.dto.OrderResponse;
import com.restaurant.ordering.model.OrderStatus;
import com.restaurant.ordering.security.service.CustomUserDetailsService;
import com.restaurant.ordering.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MVC security tests for order-status endpoints.
 *
 * <p>This test loads the real {@link SecurityConfig} together with the order
 * controller and verifies that URL-level security does not block a customer
 * cancellation request before the service layer can enforce ownership and
 * workflow rules.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    /**
     * Verifies that a CUSTOMER JWT may reach PATCH /api/orders/{id}/status.
     * The service layer remains responsible for checking that the order belongs
     * to the customer and that cancellation is allowed before READY.
     */
    @Test
    void customerCanReachOrderCancellationEndpoint() throws Exception {
        OrderResponse cancelledOrder = OrderResponse.builder()
                .id(1L)
                .status(OrderStatus.CANCELLED)
                .build();
        when(orderService.updateStatus(1L, OrderStatus.CANCELLED)).thenReturn(cancelledOrder);

        mockMvc.perform(patch("/api/orders/1/status")
                        .param("value", "CANCELLED")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
                .andExpect(status().isOk());

        verify(orderService).updateStatus(1L, OrderStatus.CANCELLED);
    }
}
