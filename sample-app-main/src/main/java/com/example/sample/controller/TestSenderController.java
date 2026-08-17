package com.example.sample.controller;

import com.example.messaging.core.spi.DeliveryMode;
import com.example.messaging.core.spi.MessagePublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TestSenderController {

    private final MessagePublisher messagePublisher;
    private final ObjectMapper objectMapper;

    public TestSenderController(MessagePublisher messagePublisher, ObjectMapper objectMapper) {
        this.messagePublisher = messagePublisher;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/test/send-webhook")
    public String sendWebhook(
            @RequestParam("messageType") String messageType,
            @RequestParam("targetUrl") String targetUrl,
            @RequestBody Map<String, Object> payload) {
        try {
            Map<String, Object> enrichedPayload = new HashMap<>(payload);
            enrichedPayload.put("targetUrl", targetUrl);

            String serializedPayload = objectMapper.writeValueAsString(enrichedPayload);
            messagePublisher.publish(messageType, serializedPayload, DeliveryMode.AT_LEAST_ONCE);

            return "Success";
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish message", e);
        }
    }
}
