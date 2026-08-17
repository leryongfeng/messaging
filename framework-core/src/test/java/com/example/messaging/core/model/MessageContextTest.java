package com.example.messaging.core.model;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MessageContextTest {

    @Test
    public void testGettersSettersAndBuilder() {
        Map<String, String> headers = new HashMap<>();
        headers.put("key", "value");

        MessageContext context = MessageContext.builder()
                .messageId("msg-1")
                .payload("payload")
                .messageType("type")
                .aggregateId("agg-1")
                .aggregateVersion(2L)
                .sequenceNumber(3L)
                .headers(headers)
                .build();

        assertEquals("msg-1", context.getMessageId());
        assertEquals("payload", context.getPayload());
        assertEquals("type", context.getMessageType());
        assertEquals("agg-1", context.getAggregateId());
        assertEquals(2L, context.getAggregateVersion());
        assertEquals(3L, context.getSequenceNumber());
        assertEquals(headers, context.getHeaders());

        // Test setters
        context.setMessageId("msg-2");
        context.setPayload("payload2");
        context.setMessageType("type2");
        context.setAggregateId("agg-2");
        context.setAggregateVersion(4L);
        context.setSequenceNumber(5L);
        Map<String, String> headers2 = new HashMap<>();
        context.setHeaders(headers2);

        assertEquals("msg-2", context.getMessageId());
        assertEquals("payload2", context.getPayload());
        assertEquals("type2", context.getMessageType());
        assertEquals("agg-2", context.getAggregateId());
        assertEquals(4L, context.getAggregateVersion());
        assertEquals(5L, context.getSequenceNumber());
        assertEquals(headers2, context.getHeaders());
    }

    @Test
    public void testEqualsAndHashCode() {
        Map<String, String> headers = new HashMap<>();
        headers.put("key", "value");

        MessageContext context1 = new MessageContext("msg", "payload", "type", "agg", 1L, 1L, headers);
        MessageContext context2 = new MessageContext("msg", "payload", "type", "agg", 1L, 1L, headers);
        MessageContext context3 = new MessageContext("msg2", "payload", "type", "agg", 1L, 1L, headers);

        assertEquals(context1, context1);
        assertEquals(context1, context2);
        assertNotEquals(context1, context3);
        assertNotEquals(context1, null);
        assertNotEquals(context1, "string");

        assertEquals(context1.hashCode(), context2.hashCode());
        assertNotEquals(context1.hashCode(), context3.hashCode());
    }

    @Test
    public void testToString() {
        MessageContext context = new MessageContext("msg", "payload", "type", "agg", 1L, 1L, null);
        String str = context.toString();
        assertTrue(str.contains("msg"));
        assertTrue(str.contains("payload"));
        assertTrue(str.contains("type"));
        assertTrue(str.contains("agg"));
    }
}
