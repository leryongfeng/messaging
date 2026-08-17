package com.example.sample;

import com.example.messaging.core.spi.MessagePublisher;
import com.example.messaging.core.webhook.ExternalWebhookDispatcher;
import com.example.messaging.rdbms.poller.OutboxPollingEngine;
import com.example.messaging.rdbms.resolver.AggregateVersionResolver;
import com.example.messaging.rdbms.router.MessageRouter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {TestApplication.class, BrokerlessStarterIntegrationIT.TestConfig.class})
public class BrokerlessStarterIntegrationIT {

    @Autowired
    private ApplicationContext context;

    @TestConfiguration
    public static class TestConfig {
        @Bean
        public AggregateVersionResolver customAggregateVersionResolver() {
            return aggregateId -> 100L;
        }
    }

    @Test
    void contextLoadsAndInjectsFrameworkBeans() {
        // Assert framework auto-configured components are present in the context
        assertThat(context.getBean(MessageRouter.class)).isNotNull();
        assertThat(context.getBean(OutboxPollingEngine.class)).isNotNull();
        assertThat(context.getBean(MessagePublisher.class)).isNotNull();
        assertThat(context.getBean(ExternalWebhookDispatcher.class)).isNotNull();
    }

    @Test
    void customBeansOverrideFrameworkDefaults() {
        // Assert custom resolver bean successfully overrides default matching
        AggregateVersionResolver resolver = context.getBean(AggregateVersionResolver.class);
        assertThat(resolver).isNotNull();
        assertThat(resolver.getCurrentVersion("any-id")).isEqualTo(100L);
    }
}
