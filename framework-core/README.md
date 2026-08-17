# Framework Core Module

Contains the API and Service Provider Abstractions (SPI) for event publishing, routing, and generic webhook dispatching.

## 1. SPI Contracts

### MessagePublisher
Inject this interface in your services to queue messages into the transactional outbox:
```java
public interface MessagePublisher {
    void publish(String messageType, String payload, DeliveryMode mode);
}
```

### MessageContext
Encapsulates metadata containing message payload, headers, aggregate context, and sequencing details:
```java
MessageContext context = MessageContext.builder()
    .messageId("msg-1")
    .messageType("ORDER_SHIPPED")
    .payload("{\"orderId\": 99}")
    .build();
```

## 2. Event Handlers
Implement `EventHandler` to handle specific incoming types:
```java
@Component
public class OrderShippedHandler implements EventHandler {
    @Override
    public boolean supports(String messageType) {
        return "ORDER_SHIPPED".equals(messageType);
    }

    @Override
    public void handle(MessageContext context) throws Exception {
        // Business logic execution
    }
}
```

## 3. Webhook Dispatcher
Features a built-in `ExternalWebhookDispatcher` which routes webhook events with circuitbreaker constraints and Mutual TLS (mTLS) configurations.
