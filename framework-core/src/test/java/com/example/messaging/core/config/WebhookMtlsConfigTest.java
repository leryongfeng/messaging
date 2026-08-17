package com.example.messaging.core.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.*;

public class WebhookMtlsConfigTest {

    @Test
    public void testMtlsWebClientWithoutKeystore() {
        WebhookMtlsProperties properties = new WebhookMtlsProperties();
        WebhookMtlsConfig config = new WebhookMtlsConfig(properties);

        WebClient webClient = config.mtlsWebClient();
        assertNotNull(webClient);
    }

    @Test
    public void testMtlsWebClientWithBlankKeystore() {
        WebhookMtlsProperties properties = new WebhookMtlsProperties();
        properties.setKeystorePath("   ");
        WebhookMtlsConfig config = new WebhookMtlsConfig(properties);

        WebClient webClient = config.mtlsWebClient();
        assertNotNull(webClient);
    }

    @Test
    public void testMtlsWebClientWithValidKeystore() {
        WebhookMtlsProperties properties = new WebhookMtlsProperties();
        properties.setKeystorePath("classpath:keystore.p12");
        properties.setKeystorePassword("password");
        properties.setTruststorePath("classpath:keystore.p12");
        properties.setTruststorePassword("password");

        WebhookMtlsConfig config = new WebhookMtlsConfig(properties);
        WebClient webClient = config.mtlsWebClient();
        assertNotNull(webClient);
    }

    @Test
    public void testMtlsWebClientWithInvalidClasspathKeystoreThrowsException() {
        WebhookMtlsProperties properties = new WebhookMtlsProperties();
        properties.setKeystorePath("classpath:non_existent.p12");
        properties.setKeystorePassword("password");

        WebhookMtlsConfig config = new WebhookMtlsConfig(properties);
        assertThrows(IllegalStateException.class, () -> {
            config.mtlsWebClient();
        });
    }

    @Test
    public void testMtlsWebClientWithInvalidFilePathKeystoreThrowsException() {
        WebhookMtlsProperties properties = new WebhookMtlsProperties();
        properties.setKeystorePath("/non/existent/file.p12");
        properties.setKeystorePassword("password");

        WebhookMtlsConfig config = new WebhookMtlsConfig(properties);
        assertThrows(IllegalStateException.class, () -> {
            config.mtlsWebClient();
        });
    }
}
