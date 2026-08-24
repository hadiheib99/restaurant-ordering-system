package com.restaurant.ordering.model;

/**
 * Authorization roles supported by the Restaurant Ordering System.
 *
 * <p>Roles are stored on user accounts and included in JWT tokens so Spring
 * Security and the Angular client can apply role-specific permissions.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
public enum Role {
    /** Full administrative access to management operations and valid status transitions. */
    ADMIN,
    /** Handles ready orders, service, payment and permitted cancellation. */
    WAITER,
    /** Browses the menu, creates orders and views/cancels own eligible orders. */
    CUSTOMER,
    /** Handles kitchen transitions from NEW to PREPARING to READY. */
    CHEF
}
