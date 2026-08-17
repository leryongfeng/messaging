package com.example.messaging.core;

import com.example.messaging.core.model.MessageContext;
import com.example.messaging.core.spi.EventHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MessageRouterTest {

    private EventHandler webhookHandler;
    private EventHandler dataDumpHandler;
    private MessageRouter messageRouter;

    @BeforeEach
    public void setUp() {
        webhookHandler = mock(EventHandler.class);
        dataDumpHandler = mock(EventHandler.class);

        when(webhookHandler.supports("WEBHOOK_OUT")).thenReturn(true);
        when(webhookHandler.supports("DATA_DUMP")).thenReturn(false);

        when(dataDumpHandler.supports("WEBHOOK_OUT")).thenReturn(false);
        when(dataDumpHandler.supports("DATA_DUMP")).thenReturn(true);

        messageRouter = new MessageRouter(Arrays.asList(webhookHandler, dataDumpHandler));
    }

    @Test
    public void testRouteWebhookOutExecutesCorrectHandler() throws Exception {
        MessageContext context = MessageContext.builder()
                .messageType("WEBHOOK_OUT")
                .payload("{\"url\":\"https://example.com\"}")
                .headers(new HashMap<>())
                .build();

        messageRouter.route(context);

        verify(webhookHandler, times(1)).handle(context);
        verify(dataDumpHandler, never()).handle(any());
    }

    @Test
    public void testRouteDataDumpExecutesCorrectHandler() throws Exception {
        MessageContext context = MessageContext.builder()
                .messageType("DATA_DUMP")
                .payload("SELECT * FROM USERS")
                .headers(new HashMap<>())
                .build();

        messageRouter.route(context);

        verify(dataDumpHandler, times(1)).handle(context);
        verify(webhookHandler, never()).handle(any());
    }

    @Test
    public void testRouteUnsupportedTypeThrowsIllegalArgumentException() {
        MessageContext context = MessageContext.builder()
                .messageType("UNSUPPORTED_TYPE")
                .payload("Some payload")
                .headers(new HashMap<>())
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            messageRouter.route(context);
        });

        assertEquals("No EventHandler registered for message type: UNSUPPORTED_TYPE", exception.getMessage());
    }

    @Test
    public void testRouteNullContextThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            messageRouter.route(null);
        });
        assertEquals("Message context or message type cannot be null", exception.getMessage());
    }

    @Test
    public void testRouteNullMessageTypeThrowsIllegalArgumentException() {
        MessageContext context = MessageContext.builder()
                .messageType(null)
                .build();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            messageRouter.route(context);
        });
        assertEquals("Message context or message type cannot be null", exception.getMessage());
    }
}
