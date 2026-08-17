package com.example.messaging.core.spi;

public interface MessagePublisher {
    void publish(String messageType, String payload, DeliveryMode mode);
}
