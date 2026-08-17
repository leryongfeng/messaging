package com.example.messaging.core.webhook;

import com.example.messaging.core.exception.RetriableDeliveryException;
import com.example.messaging.core.model.MessageContext;
import com.example.messaging.core.WebhookTestApplication;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = WebhookTestApplication.class, properties = {
    "framework.messaging.webhook.mtls.keystore-path=classpath:keystore.p12",
    "framework.messaging.webhook.mtls.keystore-password=password",
    "framework.messaging.webhook.mtls.truststore-path=classpath:keystore.p12",
    "framework.messaging.webhook.mtls.truststore-password=password"
})
public class ExternalWebhookDispatcherIT {

    private WireMockServer wireMockServer;

    @Autowired
    private ExternalWebhookDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        URL resource = getClass().getClassLoader().getResource("keystore.p12");
        if (resource == null) {
            throw new IllegalStateException("keystore.p12 not found on classpath");
        }
        String keystorePath = Paths.get(resource.toURI()).toAbsolutePath().toString();

        wireMockServer = new WireMockServer(wireMockConfig()
                .dynamicHttpsPort()
                .needClientAuth(true)
                .keystorePath(keystorePath)
                .keystorePassword("password")
                .trustStorePath(keystorePath)
                .trustStorePassword("password")
        );
        wireMockServer.start();
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void testWebhookMtlsSuccess() throws Exception {
        // Given
        String path = "/webhook";
        String targetUrl = "https://localhost:" + wireMockServer.httpsPort() + path;

        wireMockServer.stubFor(post(urlEqualTo(path))
                .willReturn(aResponse()
                        .withStatus(200)));

        Map<String, String> headers = new HashMap<>();
        headers.put("url", targetUrl);

        MessageContext context = MessageContext.builder()
                .messageId("msg-abc-123")
                .messageType("OUTBOUND_WEBHOOK")
                .payload("{\"event\":\"test\"}")
                .aggregateId("agg-billing-1")
                .aggregateVersion(5L)
                .sequenceNumber(42L)
                .headers(headers)
                .build();

        // When
        dispatcher.handle(context);

        // Then
        wireMockServer.verify(postRequestedFor(urlEqualTo(path))
                .withHeader("Idempotency-Key", equalTo("msg-abc-123"))
                .withHeader("X-Sequence-Number", equalTo("42"))
                .withHeader("X-Aggregate-Id", equalTo("agg-billing-1"))
                .withRequestBody(containing("{\"event\":\"test\"}"))
        );
    }

    @Test
    void testWebhookMtlsRetryOn503() {
        // Given
        String path = "/webhook-retry";
        String targetUrl = "https://localhost:" + wireMockServer.httpsPort() + path;

        wireMockServer.stubFor(post(urlEqualTo(path))
                .willReturn(aResponse()
                        .withStatus(503)));

        Map<String, String> headers = new HashMap<>();
        headers.put("url", targetUrl);

        MessageContext context = MessageContext.builder()
                .messageId("msg-retry-123")
                .messageType("OUTBOUND_WEBHOOK")
                .payload("{\"event\":\"retry\"}")
                .headers(headers)
                .build();

        // When/Then
        assertThrows(RetriableDeliveryException.class, () -> {
            dispatcher.handle(context);
        });
    }
}
