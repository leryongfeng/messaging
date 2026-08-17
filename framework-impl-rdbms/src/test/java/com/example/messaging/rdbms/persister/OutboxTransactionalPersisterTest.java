package com.example.messaging.rdbms.persister;

import com.example.messaging.core.spi.DeliveryMode;
import com.example.messaging.rdbms.entity.AbstractOutboxEntity;
import com.example.messaging.rdbms.event.InternalOutboxEvent;
import com.example.messaging.rdbms.repository.AuditOutboxEntity;
import com.example.messaging.rdbms.repository.AuditOutboxRepository;
import com.example.messaging.rdbms.repository.BaseOutboxRepository;
import com.example.messaging.rdbms.router.OutboxRepositoryRouter;
import org.junit.jupiter.api.Test;
import org.springframework.core.GenericTypeResolver;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OutboxTransactionalPersisterTest {

    public static abstract class PrivateOutboxEntity extends AbstractOutboxEntity {
        private PrivateOutboxEntity() {}
    }

    public interface PrivateOutboxRepository extends BaseOutboxRepository<PrivateOutboxEntity> {}

    @Test
    @SuppressWarnings("unchecked")
    public void testHandleOutboxEventSuccess() {
        OutboxRepositoryRouter router = mock(OutboxRepositoryRouter.class);
        AuditOutboxRepository mockRepo = mock(AuditOutboxRepository.class);
        when(router.resolveRepository("AUDIT")).thenReturn(mockRepo);

        OutboxTransactionalPersister persister = new OutboxTransactionalPersister(router);

        InternalOutboxEvent event = new InternalOutboxEvent(this, "AUDIT", "payload-123", DeliveryMode.AT_LEAST_ONCE);
        persister.handleOutboxEvent(event);

        verify(mockRepo, times(1)).save(any(AuditOutboxEntity.class));
    }

    @Test
    public void testHandleOutboxEventNoRepositoryThrowsException() {
        OutboxRepositoryRouter router = mock(OutboxRepositoryRouter.class);
        when(router.resolveRepository("UNKNOWN")).thenReturn(null);

        OutboxTransactionalPersister persister = new OutboxTransactionalPersister(router);
        InternalOutboxEvent event = new InternalOutboxEvent(this, "UNKNOWN", "payload", DeliveryMode.AT_LEAST_ONCE);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            persister.handleOutboxEvent(event);
        });
        assertTrue(ex.getMessage().contains("No outbox repository found"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testHandleOutboxEventInstantiationFailureThrowsRuntimeException() {
        OutboxRepositoryRouter router = mock(OutboxRepositoryRouter.class);
        PrivateOutboxRepository mockRepo = mock(PrivateOutboxRepository.class);
        when(router.resolveRepository("PRIVATE")).thenReturn(mockRepo);

        OutboxTransactionalPersister persister = new OutboxTransactionalPersister(router);
        InternalOutboxEvent event = new InternalOutboxEvent(this, "PRIVATE", "payload", DeliveryMode.AT_LEAST_ONCE);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            persister.handleOutboxEvent(event);
        });
        assertTrue(ex.getMessage().contains("Failed to instantiate and persist outbox entity"));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testResolveEntityClassFailsAndThrowsIllegalStateException() {
        OutboxRepositoryRouter router = mock(OutboxRepositoryRouter.class);
        BaseOutboxRepository mockRepo = mock(BaseOutboxRepository.class); 
        when(router.resolveRepository("GENERIC")).thenReturn(mockRepo);

        OutboxTransactionalPersister persister = new OutboxTransactionalPersister(router);
        InternalOutboxEvent event = new InternalOutboxEvent(this, "GENERIC", "payload", DeliveryMode.AT_LEAST_ONCE);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            persister.handleOutboxEvent(event);
        });
        assertTrue(ex.getMessage().contains("Could not resolve entity class for repository"));
    }
}
