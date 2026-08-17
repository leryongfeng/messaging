package com.example.sample.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestReceiverController {

    private static final Logger log = LoggerFactory.getLogger(TestReceiverController.class);

    @PostMapping("/receiver/webhook")
    public String receiveWebhook(
            @RequestBody String body,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestHeader(value = "X-Aggregate-Id", required = false) String aggregateId,
            @RequestHeader(value = "X-Sequence-Number", required = false) String sequenceNumber) {

        log.info("=== Received Webhook Block ===\n" +
                "Headers:\n" +
                "  Idempotency-Key   : {}\n" +
                "  X-Aggregate-Id    : {}\n" +
                "  X-Sequence-Number: {}\n" +
                "Payload:\n" +
                "{}\n" +
                "=============================", 
                idempotencyKey, aggregateId, sequenceNumber, body);

        return "OK";
    }
}
