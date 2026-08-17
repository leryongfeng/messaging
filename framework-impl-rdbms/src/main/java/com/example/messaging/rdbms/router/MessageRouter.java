package com.example.messaging.rdbms.router;

import com.example.messaging.core.model.MessageContext;
import com.example.messaging.core.spi.EventHandler;
import com.example.messaging.rdbms.entity.AbstractOutboxEntity;
import com.example.messaging.rdbms.entity.OutboxStatus;
import com.example.messaging.rdbms.repository.BaseOutboxRepository;
import com.example.messaging.rdbms.resolver.AggregateVersionResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;

@Component("rdbmsMessageRouter")
public class MessageRouter {

    private final List<EventHandler> handlers;
    private final AggregateVersionResolver versionResolver;

    public MessageRouter(List<EventHandler> handlers,
                         @Autowired(required = false) AggregateVersionResolver versionResolver) {
        this.handlers = handlers;
        this.versionResolver = versionResolver;
    }

    public <T extends AbstractOutboxEntity> void route(T message, BaseOutboxRepository<T> repository) {
        if (message == null) {
            return;
        }

        // Find supporting handler
        EventHandler targetHandler = null;
        for (EventHandler handler : handlers) {
            if (handler.supports(message.getMessageType())) {
                targetHandler = handler;
                break;
            }
        }

        if (targetHandler == null) {
            handleFailure(message, repository, new IllegalArgumentException("No EventHandler registered for message type: " + message.getMessageType()));
            return;
        }

        // Stale Data Check
        if (message.getAggregateId() != null && message.getAggregateVersion() != null && versionResolver != null) {
            try {
                long currentVersion = versionResolver.getCurrentVersion(message.getAggregateId());
                if (message.getAggregateVersion() < currentVersion) {
                    message.setStatus(OutboxStatus.SUPERSEDED);
                    repository.save(message);
                    return; // Return immediately without executing the handler
                }
            } catch (Exception e) {
                handleFailure(message, repository, e);
                return;
            }
        }

        // Execution & Status
        try {
            message.setStatus(OutboxStatus.PROCESSING);
            repository.saveAndFlush(message);

            MessageContext context = MessageContext.builder()
                    .messageId(message.getMessageId() != null ? message.getMessageId().toString() : null)
                    .messageType(message.getMessageType())
                    .payload(message.getPayload())
                    .aggregateId(message.getAggregateId())
                    .aggregateVersion(message.getAggregateVersion())
                    .sequenceNumber(message.getSequenceNumber())
                    .headers(new java.util.HashMap<>())
                    .build();

            targetHandler.handle(context);

            message.setStatus(OutboxStatus.COMPLETED);
            repository.save(message);
        } catch (Exception e) {
            handleFailure(message, repository, e);
        }
    }

    private <T extends AbstractOutboxEntity> void handleFailure(T message, BaseOutboxRepository<T> repository, Exception e) {
        try {
            int newRetryCount = message.getRetryCount() + 1;
            message.setRetryCount(newRetryCount);

            // Exponential backoff: 2^retryCount seconds
            long backoffSeconds = (long) Math.pow(2, newRetryCount);
            message.setNextRetryAt(Instant.now().plusSeconds(backoffSeconds));
            message.setStatus(OutboxStatus.FAILED);

            repository.save(message);
        } catch (Exception ex) {
            // Fail-safe to avoid throwing exceptions up to the polling thread
        }
    }
}
