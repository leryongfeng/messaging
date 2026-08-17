package com.example.messaging.rdbms.publisher;

import com.example.messaging.core.spi.DeliveryMode;
import com.example.messaging.rdbms.event.InternalOutboxEvent;
import com.example.messaging.rdbms.router.OutboxRepositoryRouter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class RdbmsMessagePublisherTest {

    private OutboxRepositoryRouter repositoryRouter;
    private ObjectMapper objectMapper;
    private ApplicationEventPublisher eventPublisher;
    private RdbmsMessagePublisher publisher;

    @BeforeEach
    void setUp() {
        repositoryRouter = mock(OutboxRepositoryRouter.class);
        objectMapper = mock(ObjectMapper.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        publisher = new RdbmsMessagePublisher(repositoryRouter, objectMapper, eventPublisher);
    }

    @Test
    void testPublishFiresInternalOutboxEvent() {
        // Given
        String messageType = "BILLING_INVOICE";
        String payload = "{\"invoiceId\":123}";
        DeliveryMode mode = DeliveryMode.AT_LEAST_ONCE;

        // When
        publisher.publish(messageType, payload, mode);

        // Then
        ArgumentCaptor<InternalOutboxEvent> eventCaptor = ArgumentCaptor.forClass(InternalOutboxEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        InternalOutboxEvent event = eventCaptor.getValue();
        assertThat(event.getMessageType()).isEqualTo(messageType);
        assertThat(event.getPayload()).isEqualTo(payload);
        assertThat(event.getDeliveryMode()).isEqualTo(mode);
    }
}
