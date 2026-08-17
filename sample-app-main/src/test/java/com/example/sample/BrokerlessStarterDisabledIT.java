package com.example.sample;

import com.example.messaging.rdbms.poller.OutboxPollingEngine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = TestApplication.class, properties = "brokerless.messaging.enabled=false")
public class BrokerlessStarterDisabledIT {

    @Autowired
    private ApplicationContext context;

    @Test
    void pollingEngineIsDisabled() {
        assertThrows(NoSuchBeanDefinitionException.class, () -> {
            context.getBean(OutboxPollingEngine.class);
        });

        assertThat(context.getBeansOfType(OutboxPollingEngine.class)).isEmpty();
    }
}
