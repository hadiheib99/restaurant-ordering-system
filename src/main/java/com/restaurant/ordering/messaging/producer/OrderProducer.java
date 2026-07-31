package com.restaurant.ordering.messaging.producer;

import com.restaurant.ordering.config.JmsConfig;
import com.restaurant.ordering.messaging.dto.OrderMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderProducer {

    private final JmsTemplate jmsTemplate;

    public void sendOrder(OrderMessage orderMessage) {

        jmsTemplate.convertAndSend(
                JmsConfig.KITCHEN_QUEUE,
                orderMessage
        );

        System.out.println("Order sent to kitchen: " + orderMessage.getOrderId());
    }
}