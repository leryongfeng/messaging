package com.example.messaging.core.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RetriableDeliveryExceptionTest {

    @Test
    public void testExceptionConstructors() {
        RetriableDeliveryException ex1 = new RetriableDeliveryException("error message");
        assertEquals("error message", ex1.getMessage());
        assertNull(ex1.getCause());

        Throwable cause = new RuntimeException("cause");
        RetriableDeliveryException ex2 = new RetriableDeliveryException("error message", cause);
        assertEquals("error message", ex2.getMessage());
        assertEquals(cause, ex2.getCause());
    }
}
