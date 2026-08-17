package com.example.messaging.rdbms.config;

import com.example.messaging.core.spi.EventHandler;
import com.example.messaging.core.spi.MessagePublisher;
import com.example.messaging.rdbms.entity.AbstractOutboxEntity;
import com.example.messaging.rdbms.poller.OutboxPollingEngine;
import com.example.messaging.rdbms.publisher.RdbmsMessagePublisher;
import com.example.messaging.rdbms.repository.BaseOutboxRepository;
import com.example.messaging.rdbms.router.DefaultOutboxRepositoryRouter;
import com.example.messaging.rdbms.router.MessageRouter;
import com.example.messaging.rdbms.router.OutboxRepositoryRouter;
import com.example.messaging.rdbms.resolver.AggregateVersionResolver;
import com.example.messaging.core.webhook.ExternalWebhookDispatcher;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.time.Duration;
import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties(BrokerlessMessagingProperties.class)
public class BrokerlessMessagingAutoConfiguration {

    private final BrokerlessMessagingProperties properties;

    public BrokerlessMessagingAutoConfiguration(BrokerlessMessagingProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public WebClient mtlsWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(5));

        try {
            BrokerlessMessagingProperties.Mtls mtls = properties.getMtls();
            if (mtls.getKeystorePath() != null && !mtls.getKeystorePath().isBlank()) {
                KeyStore keyStore = KeyStore.getInstance("PKCS12");
                try (InputStream ksStream = getResourceStream(mtls.getKeystorePath())) {
                    keyStore.load(ksStream, mtls.getKeystorePassword().toCharArray());
                }

                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(keyStore, mtls.getKeystorePassword().toCharArray());

                KeyStore trustStore = KeyStore.getInstance("PKCS12");
                try (InputStream tsStream = getResourceStream(mtls.getTruststorePath())) {
                    trustStore.load(tsStream, mtls.getTruststorePassword().toCharArray());
                }

                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(trustStore);

                SslContext sslContext = SslContextBuilder.forClient()
                        .keyManager(kmf)
                        .trustManager(tmf)
                        .build();

                httpClient = httpClient.secure(sslSpec -> sslSpec.sslContext(sslContext));
            }
        } catch (Exception e) {
            return WebClient.builder().build();
        }

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    private InputStream getResourceStream(String path) throws Exception {
        if (path.startsWith("classpath:")) {
            String classPath = path.substring("classpath:".length());
            InputStream stream = getClass().getClassLoader().getResourceAsStream(classPath);
            if (stream == null) {
                throw new IllegalArgumentException("Resource not found: " + path);
            }
            return stream;
        }
        return Files.newInputStream(Paths.get(path));
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper defaultObjectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public OutboxRepositoryRouter defaultOutboxRepositoryRouter(
            List<BaseOutboxRepository<? extends AbstractOutboxEntity>> repositories) {
        BaseOutboxRepository<? extends AbstractOutboxEntity> defaultRepo = repositories.isEmpty() ? null : repositories.get(0);
        DefaultOutboxRepositoryRouter router = new DefaultOutboxRepositoryRouter(defaultRepo);
        for (BaseOutboxRepository<? extends AbstractOutboxEntity> repo : repositories) {
            for (Class<?> iface : repo.getClass().getInterfaces()) {
                String name = iface.getSimpleName().toUpperCase();
                router.registerRoute(name + "*", repo);
                router.registerRoute("*" + name, repo);
            }
        }
        return router;
    }

    @Bean
    @ConditionalOnMissingBean
    public MessagePublisher rdbmsMessagePublisher(
            OutboxRepositoryRouter repositoryRouter,
            ObjectMapper objectMapper,
            ApplicationEventPublisher eventPublisher) {
        return new RdbmsMessagePublisher(repositoryRouter, objectMapper, eventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean(name = "rdbmsMessageRouter")
    public MessageRouter rdbmsMessageRouter(
            List<EventHandler> handlers,
            @Autowired(required = false) AggregateVersionResolver versionResolver) {
        return new MessageRouter(handlers, versionResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public ExternalWebhookDispatcher externalWebhookDispatcher(
            WebClient mtlsWebClient,
            ObjectMapper objectMapper) {
        return new ExternalWebhookDispatcher(mtlsWebClient, objectMapper);
    }

    @Bean
    @ConditionalOnProperty(prefix = "brokerless.messaging", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public OutboxPollingEngine outboxPollingEngine(
            List<BaseOutboxRepository<? extends AbstractOutboxEntity>> repositories,
            MessageRouter messageRouter) {
        return new OutboxPollingEngine(repositories, messageRouter);
    }
}
