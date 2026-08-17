package com.example.messaging.rdbms.persister;

import com.example.messaging.rdbms.entity.AbstractOutboxEntity;
import com.example.messaging.rdbms.entity.OutboxStatus;
import com.example.messaging.rdbms.event.InternalOutboxEvent;
import com.example.messaging.rdbms.repository.BaseOutboxRepository;
import com.example.messaging.rdbms.router.OutboxRepositoryRouter;
import org.springframework.core.GenericTypeResolver;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;

@Component
public class OutboxTransactionalPersister {

    private final OutboxRepositoryRouter repositoryRouter;

    public OutboxTransactionalPersister(OutboxRepositoryRouter repositoryRouter) {
        this.repositoryRouter = repositoryRouter;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleOutboxEvent(InternalOutboxEvent event) {
        BaseOutboxRepository repository = repositoryRouter.resolveRepository(event.getMessageType());
        if (repository == null) {
            throw new IllegalArgumentException("No outbox repository found for message type: " + event.getMessageType());
        }

        Class<? extends AbstractOutboxEntity> entityClass = resolveEntityClass(repository);

        try {
            AbstractOutboxEntity entity = entityClass.getDeclaredConstructor().newInstance();
            entity.setMessageType(event.getMessageType());
            entity.setPayload(event.getPayload());
            entity.setStatus(OutboxStatus.PENDING);
            entity.setNextRetryAt(Instant.now());
            entity.setRetryCount(0);

            repository.save(entity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate and persist outbox entity for type: " + event.getMessageType(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends AbstractOutboxEntity> resolveEntityClass(BaseOutboxRepository repository) {
        for (Class<?> iface : repository.getClass().getInterfaces()) {
            if (BaseOutboxRepository.class.isAssignableFrom(iface)) {
                Class<?>[] typeArgs = GenericTypeResolver.resolveTypeArguments(iface, BaseOutboxRepository.class);
                if (typeArgs != null && typeArgs.length > 0) {
                    return (Class<? extends AbstractOutboxEntity>) typeArgs[0];
                }
            }
        }
        throw new IllegalStateException("Could not resolve entity class for repository: " + repository.getClass());
    }
}
