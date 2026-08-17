package com.example.messaging.rdbms.router;

import com.example.messaging.rdbms.repository.BaseOutboxRepository;

public interface OutboxRepositoryRouter {
    BaseOutboxRepository resolveRepository(String messageType);
}
