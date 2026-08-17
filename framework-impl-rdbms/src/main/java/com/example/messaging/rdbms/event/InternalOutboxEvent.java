package com.example.messaging.rdbms.event;

import com.example.messaging.core.spi.DeliveryMode;
import org.springframework.context.ApplicationEvent;

public class InternalOutboxEvent extends ApplicationEvent {
    private final String messageType;
    private final String payload;
    private final DeliveryMode deliveryMode;

    public InternalOutboxEvent(Object source, String messageType, String payload, DeliveryMode deliveryMode) {
        super(source);
        this.messageType = messageType;
        this.payload = payload;
        this.deliveryMode = deliveryMode;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getPayload() {
        return payload;
    }

    public DeliveryMode getDeliveryMode() {
        return deliveryMode;
    }
}
