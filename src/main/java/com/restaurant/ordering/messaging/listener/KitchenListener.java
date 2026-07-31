package com.restaurant.ordering.messaging.listener;

import com.restaurant.ordering.config.JmsConfig;
import com.restaurant.ordering.messaging.dto.OrderMessage;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component

public class KitchenListener {

    @JmsListener(destination = JmsConfig.KITCHEN_QUEUE)
    public void receiveOrder(OrderMessage message) {

        System.out.println();
        System.out.println("========================================");

        switch (message.getStatus()) {

            case "NEW" -> {
                System.out.println("🆕 New Order Received");
            }

            case "PREPARING" -> {
                System.out.println("👨‍🍳 Kitchen Started Preparing");
            }

            case "READY" -> {
                System.out.println("✅ Order Ready For Pickup");
            }

            case "SERVED" -> {
                System.out.println("🍽 Order Served");
            }

            case "PAID" -> {
                System.out.println("💰 Order Paid");
            }

            case "CANCELLED" -> {
                System.out.println("❌ Order Cancelled");
            }

            default -> {
                System.out.println("📦 Order Updated");
            }
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