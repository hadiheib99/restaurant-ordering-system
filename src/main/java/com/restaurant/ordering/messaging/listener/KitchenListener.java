package com.restaurant.ordering.messaging.listener;

import com.restaurant.ordering.config.JmsConfig;
import com.restaurant.ordering.messaging.dto.OrderMessage;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class KitchenListener {

    @JmsListener(destination = JmsConfig.KITCHEN_QUEUE)
    public void receiveOrder(OrderMessage message) {

        System.out.println("--------------------------------");
        System.out.println("Kitchen received new order");
        System.out.println("Order ID: " + message.getOrderId());
        System.out.println("Customer: " + message.getCustomerName());
        System.out.println("Total: ₪" + message.getTotalPrice());
        System.out.println("--------------------------------");
    }
}