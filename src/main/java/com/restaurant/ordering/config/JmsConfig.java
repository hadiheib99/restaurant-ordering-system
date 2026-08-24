package com.restaurant.ordering.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.JacksonJsonMessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

/**
 * Configures JMS message conversion for ActiveMQ Artemis communication.
 *
 * <p>Order events are serialized as JSON text messages so producers and the
 * kitchen listener exchange a stable, readable message format.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@Configuration
public class JmsConfig {

    /** Queue used to publish order events to the kitchen. */
    public static final String KITCHEN_QUEUE = "restaurant.kitchen.queue";

    /**
     * Creates the Jackson converter used by Spring JMS.
     * @return converter that serializes JMS payloads as JSON text messages
     */
    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }
}
