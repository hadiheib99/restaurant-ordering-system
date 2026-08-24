package com.restaurant.ordering.messaging.producer;

import com.restaurant.ordering.config.JmsConfig;
import com.restaurant.ordering.messaging.dto.OrderMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * JMS producer responsible for publishing order events to ActiveMQ Artemis.
 *
 * <p>Order creation and status changes use this component so the kitchen can be
 * notified asynchronously without coupling order business logic to a consumer.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class OrderProducer {

    private final JmsTemplate jmsTemplate;

    /**
     * Serializes and sends an order event to the configured kitchen queue.
     * @param orderMessage event payload to publish
     */
    public void sendOrderEvent(OrderMessage orderMessage) {
        jmsTemplate.convertAndSend(JmsConfig.KITCHEN_QUEUE, orderMessage);
        System.out.println("Order event sent: " + orderMessage.getOrderId()
                + " | Status: " + orderMessage.getStatus());
    }
}
