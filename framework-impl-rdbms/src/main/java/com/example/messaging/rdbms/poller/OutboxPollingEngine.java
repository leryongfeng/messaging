package com.example.messaging.rdbms.poller;

import com.example.messaging.rdbms.entity.AbstractOutboxEntity;
import com.example.messaging.rdbms.repository.BaseOutboxRepository;
import com.example.messaging.rdbms.router.MessageRouter;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class OutboxPollingEngine {

    private final List<BaseOutboxRepository<? extends AbstractOutboxEntity>> repositories;
    private final MessageRouter messageRouter;

    public OutboxPollingEngine(List<BaseOutboxRepository<? extends AbstractOutboxEntity>> repositories,
                               MessageRouter messageRouter) {
        this.repositories = repositories;
        this.messageRouter = messageRouter;
    }

    @Scheduled(fixedDelayString = "${framework.messaging.poll-interval:2000}")
    public void pollAndRouteMessages() {
        if (repositories == null || repositories.isEmpty()) {
            return;
        }

        for (BaseOutboxRepository<? extends AbstractOutboxEntity> repository : repositories) {
            try {
                List<? extends AbstractOutboxEntity> pendingMessages = repository.pollPendingMessages(Instant.now(), PageRequest.of(0, 50));
                
                for (AbstractOutboxEntity message : pendingMessages) {
                    try {
                        routeMessage(message, repository);
                    } catch (Exception e) {
                        // Safe logging or handling to prevent thread crash
                    }
                }
            } catch (Exception e) {
                // Safe logging or handling to prevent thread crash
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends AbstractOutboxEntity> void routeMessage(T message, BaseOutboxRepository<? extends AbstractOutboxEntity> repository) {
        BaseOutboxRepository<T> typedRepository = (BaseOutboxRepository<T>) repository;
        messageRouter.route(message, typedRepository);
    }
}
