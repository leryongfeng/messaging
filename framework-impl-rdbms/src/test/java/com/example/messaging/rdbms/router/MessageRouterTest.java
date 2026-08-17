package com.example.messaging.rdbms.router;

import com.example.messaging.core.model.MessageContext;
import com.example.messaging.core.spi.EventHandler;
import com.example.messaging.rdbms.entity.AbstractOutboxEntity;
import com.example.messaging.rdbms.entity.OutboxStatus;
import com.example.messaging.rdbms.repository.BaseOutboxRepository;
import com.example.messaging.rdbms.resolver.AggregateVersionResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

public class MessageRouterTest {

    private EventHandler sampleHandler;
    private AggregateVersionResolver versionResolver;
    private BaseOutboxRepository<TestEntity> repository;
    private MessageRouter router;

    public static class TestEntity extends AbstractOutboxEntity {}

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        sampleHandler = mock(EventHandler.class);
        versionResolver = mock(AggregateVersionResolver.class);
        repository = mock(BaseOutboxRepository.class);

        when(sampleHandler.supports("TEST_TYPE")).thenReturn(true);

        router = new MessageRouter(Arrays.asList(sampleHandler), versionResolver);
    }

    @Test
    void testRouteSuccessfulExecution() throws Exception {
        // Given
        TestEntity entity = new TestEntity();
        entity.setMessageType("TEST_TYPE");
        entity.setPayload("test-payload");
        entity.setAggregateId("agg-1");
        entity.setAggregateVersion(2L);

        when(versionResolver.getCurrentVersion("agg-1")).thenReturn(2L);

        // When
        router.route(entity, repository);

        // Then
        verify(sampleHandler, times(1)).handle(any(MessageContext.class));
        assertThat(entity.getStatus()).isEqualTo(OutboxStatus.COMPLETED);
        verify(repository, times(1)).save(entity);
    }

    @Test
    void testRouteSupersededExecution() throws Exception {
        // Given
        TestEntity entity = new TestEntity();
        entity.setMessageType("TEST_TYPE");
        entity.setPayload("test-payload");
        entity.setAggregateId("agg-1");
        entity.setAggregateVersion(1L);

        when(versionResolver.getCurrentVersion("agg-1")).thenReturn(2L);

        // When
        router.route(entity, repository);

        // Then
        verify(sampleHandler, never()).handle(any(MessageContext.class));
        assertThat(entity.getStatus()).isEqualTo(OutboxStatus.SUPERSEDED);
        verify(repository, times(1)).save(entity);
    }

    @Test
    void testRouteFailedExecutionExponentialBackoff() throws Exception {
        // Given
        TestEntity entity = new TestEntity();
        entity.setMessageType("TEST_TYPE");
        entity.setPayload("test-payload");
        entity.setAggregateId("agg-1");
        entity.setAggregateVersion(2L);
        entity.setRetryCount(1);

        when(versionResolver.getCurrentVersion("agg-1")).thenReturn(2L);
        doThrow(new RuntimeException("Handler error")).when(sampleHandler).handle(any(MessageContext.class));

        Instant beforeRoute = Instant.now();

        // When
        router.route(entity, repository);

        // Then
        verify(sampleHandler, times(1)).handle(any(MessageContext.class));
        assertThat(entity.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(entity.getRetryCount()).isEqualTo(2);
        assertThat(entity.getNextRetryAt()).isAfterOrEqualTo(beforeRoute.plusSeconds(4));
        verify(repository, times(1)).save(entity);
    }

    @Test
    void testRouteNullMessageDoesNothing() {
        router.route(null, repository);
        verify(repository, never()).save(any());
        verify(sampleHandler, never()).supports(any());
    }

    @Test
    void testRouteNoHandlerFoundTriggersFailure() {
        TestEntity entity = new TestEntity();
        entity.setMessageType("UNSUPPORTED_TYPE");
        entity.setRetryCount(0);

        router.route(entity, repository);

        assertThat(entity.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(entity.getRetryCount()).isEqualTo(1);
        verify(repository, times(1)).save(entity);
    }

    @Test
    void testRouteVersionResolverThrowsException() {
        TestEntity entity = new TestEntity();
        entity.setMessageType("TEST_TYPE");
        entity.setAggregateId("agg-1");
        entity.setAggregateVersion(1L);
        entity.setRetryCount(0);

        when(versionResolver.getCurrentVersion("agg-1")).thenThrow(new RuntimeException("Version resolution error"));

        router.route(entity, repository);

        assertThat(entity.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(entity.getRetryCount()).isEqualTo(1);
        verify(repository, times(1)).save(entity);
    }

    @Test
    void testRouteFailureExceptionInRepoSaveDoesNotCrash() {
        TestEntity entity = new TestEntity();
        entity.setMessageType("TEST_TYPE");
        entity.setRetryCount(0);

        // Repo save throws exception during failure handling
        doThrow(new RuntimeException("DB offline on save failure")).when(repository).save(entity);

        // Trigger failure by omitting target handler configurations or version resolution failure
        router.route(entity, repository);

        // This call should complete without throwing any exception since handleFailure catches exception silently
        assertDoesNotThrow(() -> {
            router.route(entity, repository);
        });
    }
}
