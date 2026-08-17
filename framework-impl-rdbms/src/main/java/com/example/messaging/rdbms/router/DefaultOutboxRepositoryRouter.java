package com.example.messaging.rdbms.router;

import com.example.messaging.rdbms.repository.BaseOutboxRepository;
import org.springframework.util.AntPathMatcher;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultOutboxRepositoryRouter implements OutboxRepositoryRouter {

    private final Map<String, BaseOutboxRepository> routes = new ConcurrentHashMap<>();
    private BaseOutboxRepository defaultRepository;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public DefaultOutboxRepositoryRouter() {
    }

    public DefaultOutboxRepositoryRouter(BaseOutboxRepository defaultRepository) {
        this.defaultRepository = defaultRepository;
    }

    public void registerRoute(String pattern, BaseOutboxRepository repository) {
        routes.put(pattern, repository);
    }

    public void setDefaultRepository(BaseOutboxRepository defaultRepository) {
        this.defaultRepository = defaultRepository;
    }

    @Override
    public BaseOutboxRepository resolveRepository(String messageType) {
        if (messageType != null) {
            for (Map.Entry<String, BaseOutboxRepository> entry : routes.entrySet()) {
                if (pathMatcher.match(entry.getKey(), messageType)) {
                    return entry.getValue();
                }
            }
        }
        return defaultRepository;
    }
}
