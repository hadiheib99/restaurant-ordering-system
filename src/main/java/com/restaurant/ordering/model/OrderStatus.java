package com.restaurant.ordering.model;

/**
 * Lifecycle states supported by a restaurant order.
 *
 * <p>The normal flow is NEW -&gt; PREPARING -&gt; READY -&gt; SERVED -&gt; PAID.
 * NEW and PREPARING orders may also transition to CANCELLED when the current
 * user's role and ownership rules permit cancellation.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
public enum OrderStatus {
    /** Order was created and is waiting for kitchen action. */
    NEW,
    /** Kitchen has started preparing the order. */
    PREPARING,
    /** Kitchen has finished preparation and the order awaits service. */
    READY,
    /** Waiter delivered the order to the customer. */
    SERVED,
    /** Payment was completed. */
    PAID,
    /** Order was cancelled before reaching the ready state. */
    CANCELLED
}
