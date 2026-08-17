package com.example.messaging.rdbms.router;

import com.example.messaging.rdbms.repository.BaseOutboxRepository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DefaultOutboxRepositoryRouterTest {

    @Test
    @SuppressWarnings("rawtypes")
    public void testRouterMatchingAndResolving() {
        BaseOutboxRepository defaultRepo = mock(BaseOutboxRepository.class);
        BaseOutboxRepository auditRepo = mock(BaseOutboxRepository.class);

        DefaultOutboxRepositoryRouter router = new DefaultOutboxRepositoryRouter(defaultRepo);

        // Assert empty map returns default repo
        assertEquals(defaultRepo, router.resolveRepository("ANY_TYPE"));
        assertEquals(defaultRepo, router.resolveRepository(null));

        // Register route and test matching
        router.registerRoute("AUDIT_*", auditRepo);
        assertEquals(auditRepo, router.resolveRepository("AUDIT_LOG"));
        assertEquals(defaultRepo, router.resolveRepository("OTHER_TYPE"));

        // Match null type returning default
        assertEquals(defaultRepo, router.resolveRepository(null));

        // Test custom constructor and setter
        DefaultOutboxRepositoryRouter router2 = new DefaultOutboxRepositoryRouter();
        assertNull(router2.resolveRepository("ANY"));
        router2.setDefaultRepository(defaultRepo);
        assertEquals(defaultRepo, router2.resolveRepository("ANY"));
    }
}
