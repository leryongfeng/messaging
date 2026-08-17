package com.example.messaging.rdbms.publisher;

import com.example.messaging.core.spi.DeliveryMode;
import com.example.messaging.core.spi.MessagePublisher;
import com.example.messaging.rdbms.event.InternalOutboxEvent;
import com.example.messaging.rdbms.router.OutboxRepositoryRouter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class RdbmsMessagePublisher implements MessagePublisher {

    private final OutboxRepositoryRouter repositoryRouter;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    public RdbmsMessagePublisher(OutboxRepositoryRouter repositoryRouter,
                                 ObjectMapper objectMapper,
                                 ApplicationEventPublisher eventPublisher) {
        this.repositoryRouter = repositoryRouter;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publish(String messageType, String payload, DeliveryMode mode) {
        if (messageType == null) {
            throw new IllegalArgumentException("Message type cannot be null");
        }
        InternalOutboxEvent event = new InternalOutboxEvent(this, messageType, payload, mode);
        eventPublisher.publishEvent(event);
    }
}
