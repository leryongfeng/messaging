package com.example.sample.controller;

import com.example.messaging.core.spi.MessagePublisher;
import com.example.sample.TestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.springframework.context.annotation.Import;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureWebTestClient
@Import({TestSenderController.class, TestReceiverController.class})
public class ControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MessagePublisher messagePublisher;

    @Test
    public void testSendWebhook() throws Exception {
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/test/send-webhook")
                        .queryParam("messageType", "TEST_MESSAGE")
                        .queryParam("targetUrl", "http://localhost:8080/receiver/webhook")
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("key", "value"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("Success");

        verify(messagePublisher).publish(eq("TEST_MESSAGE"), any(String.class), any());
    }

    @Test
    public void testReceiveWebhook() throws Exception {
        webTestClient.post()
                .uri("/receiver/webhook")
                .header("Idempotency-Key", "idemp-key-123")
                .header("X-Aggregate-Id", "agg-456")
                .header("X-Sequence-Number", "12")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"event\":\"test\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("OK");
    }
}
