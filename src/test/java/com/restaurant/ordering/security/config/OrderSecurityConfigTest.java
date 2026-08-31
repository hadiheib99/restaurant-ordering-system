package com.restaurant.ordering.security.config;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression tests for order URL-level role configuration.
 *
 * <p>The service test suite separately verifies customer ownership and lifecycle
 * rules. This test locks the production PATCH role list so a customer cannot be
 * accidentally removed from the security layer again.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
class OrderSecurityConfigTest {

    /** Verifies that all intended roles, including CUSTOMER, may reach order-status PATCH endpoints. */
    @Test
    void orderPatchRolesIncludeCustomerCancellationAccess() {
        Set<String> expectedRoles = Set.of("ADMIN", "WAITER", "CHEF", "CUSTOMER");
        Set<String> configuredRoles = Set.copyOf(Arrays.asList(SecurityConfig.ORDER_PATCH_ROLES));

        assertEquals(expectedRoles, configuredRoles);
    }
}
