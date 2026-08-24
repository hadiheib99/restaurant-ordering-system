package com.restaurant.ordering.messaging.listener;

import com.restaurant.ordering.config.JmsConfig;
import com.restaurant.ordering.messaging.dto.OrderMessage;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * JMS consumer that receives restaurant order events for the kitchen.
 *
 * <p>The listener demonstrates asynchronous JMS communication by consuming
 * {@link OrderMessage} values from the kitchen queue and logging a clear status
 * notification for each event.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@Component
public class KitchenListener {

    /**
     * Handles one order event delivered by ActiveMQ Artemis.
     * @param message deserialized order event received from the kitchen queue
     */
    @JmsListener(destination = JmsConfig.KITCHEN_QUEUE)
    public void receiveOrder(OrderMessage message) {
        System.out.println();
        System.out.println("========================================");

        switch (message.getStatus()) {
            case "NEW" -> System.out.println("🆕 New Order Received");
            case "PREPARING" -> System.out.println("👨‍🍳 Kitchen Started Preparing");
            case "READY" -> System.out.println("✅ Order Ready For Pickup");
            case "SERVED" -> System.out.println("🍽 Order Served");
            case "PAID" -> System.out.println("💰 Order Paid");
            case "CANCELLED" -> System.out.println("❌ Order Cancelled");
            default -> System.out.println("📦 Order Updated");
        }

        System.out.println("----------------------------------------");
        System.out.println("Order ID : " + message.getOrderId());
        System.out.println("Customer : " + message.getCustomerName());
        System.out.println("Total    : ₪" + message.getTotalPrice());
        System.out.println("Status   : " + message.getStatus());
        System.out.println("========================================");
        System.out.println();
    }
}
