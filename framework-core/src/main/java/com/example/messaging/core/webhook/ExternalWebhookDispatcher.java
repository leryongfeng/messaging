package com.example.messaging.core.webhook;

import com.example.messaging.core.exception.RetriableDeliveryException;
import com.example.messaging.core.model.MessageContext;
import com.example.messaging.core.spi.EventHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.logging.Logger;

@Component
public class ExternalWebhookDispatcher implements EventHandler {

    private static final Logger LOGGER = Logger.getLogger(ExternalWebhookDispatcher.class.getName());

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public ExternalWebhookDispatcher(WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(String messageType) {
        return "OUTBOUND_WEBHOOK".equals(messageType);
    }

    @Override
    public void handle(MessageContext context) throws Exception {
        String targetUrl = resolveUrl(context);
        if (targetUrl == null) {
            throw new IllegalArgumentException("No target URL found in context payload or headers");
        }

        try {
            WebClient.RequestHeadersSpec<?> requestSpec = webClient.post()
                    .uri(targetUrl)
                    .bodyValue(context.getPayload() != null ? context.getPayload() : "");

            if (context.getMessageId() != null) {
                requestSpec.header("Idempotency-Key", context.getMessageId());
            }
            if (context.getSequenceNumber() != null) {
                requestSpec.header("X-Sequence-Number", String.valueOf(context.getSequenceNumber()));
            }
            if (context.getAggregateId() != null) {
                requestSpec.header("X-Aggregate-Id", context.getAggregateId());
            }

            requestSpec.retrieve()
                    .toBodilessEntity()
                    .block();

        } catch (WebClientResponseException e) {
            HttpStatusCode status = e.getStatusCode();
            if (status.value() == 429 || status.is5xxServerError()) {
                throw new RetriableDeliveryException("Retriable delivery failure: HTTP " + status.value(), e);
            } else {
                LOGGER.warning("Permanent delivery failure: HTTP " + status.value() + ". Dropping message.");
            }
        } catch (Exception e) {
            throw new RetriableDeliveryException("Retriable delivery failure: " + e.getMessage(), e);
        }
    }

    private String resolveUrl(MessageContext context) {
        if (context.getHeaders() != null) {
            String url = context.getHeaders().get("url");
            if (url != null) return url;
            url = context.getHeaders().get("targetUrl");
            if (url != null) return url;
        }

        String payload = context.getPayload();
        if (payload != null && !payload.isBlank()) {
            payload = payload.trim();
            if (payload.startsWith("http://") || payload.startsWith("https://")) {
                return payload;
            }
            try {
                JsonNode json = objectMapper.readTree(payload);
                if (json.has("url")) {
                    return json.get("url").asText();
                }
                if (json.has("targetUrl")) {
                    return json.get("targetUrl").asText();
                }
            } catch (Exception e) {
                // Fail-silent payload fallback
            }
        }
        return null;
    }
}
