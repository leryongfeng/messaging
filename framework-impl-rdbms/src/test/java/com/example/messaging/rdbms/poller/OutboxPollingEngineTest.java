package com.example.messaging.rdbms.poller;

import com.example.messaging.rdbms.entity.AbstractOutboxEntity;
import com.example.messaging.rdbms.repository.BaseOutboxRepository;
import com.example.messaging.rdbms.router.MessageRouter;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class OutboxPollingEngineTest {

    public static class DummyEntity extends AbstractOutboxEntity {}

    @Test
    @SuppressWarnings("unchecked")
    public void testPollAndRouteMessagesSuccessfulAndExceptionBranches() {
        MessageRouter mockRouter = mock(MessageRouter.class);

        // Scenario 1: Null/Empty Repositories
        OutboxPollingEngine engineNull = new OutboxPollingEngine(null, mockRouter);
        engineNull.pollAndRouteMessages(); // Should return silently

        OutboxPollingEngine engineEmpty = new OutboxPollingEngine(new ArrayList<>(), mockRouter);
        engineEmpty.pollAndRouteMessages(); // Should return silently

        // Scenario 2: Active Repositories
        BaseOutboxRepository<DummyEntity> repo1 = mock(BaseOutboxRepository.class);
        BaseOutboxRepository<DummyEntity> repo2 = mock(BaseOutboxRepository.class);

        DummyEntity msg1 = new DummyEntity();
        DummyEntity msg2 = new DummyEntity();

        // Repo 1 returns messages
        when(repo1.pollPendingMessages(any(), any())).thenReturn(List.of(msg1, msg2));
        
        // Repo 2 throws exception
        when(repo2.pollPendingMessages(any(), any())).thenThrow(new RuntimeException("DB Exception"));

        List<BaseOutboxRepository<? extends AbstractOutboxEntity>> repos = List.of(repo1, repo2);
        OutboxPollingEngine engine = new OutboxPollingEngine(repos, mockRouter);

        // Router throws exception on routing msg2
        doNothing().when(mockRouter).route(eq(msg1), eq(repo1));
        doThrow(new RuntimeException("Routing Exception")).when(mockRouter).route(eq(msg2), eq(repo1));

        engine.pollAndRouteMessages();

        // Verify route called on both messages for repo1
        verify(mockRouter, times(1)).route(msg1, repo1);
        verify(mockRouter, times(1)).route(msg2, repo1);
    }
}
