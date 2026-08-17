package com.example.messaging.core.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WebhookMtlsPropertiesTest {

    @Test
    public void testGettersAndSetters() {
        WebhookMtlsProperties props = new WebhookMtlsProperties();
        
        props.setKeystorePath("classpath:test.p12");
        props.setKeystorePassword("pass1");
        props.setTruststorePath("classpath:trust.p12");
        props.setTruststorePassword("pass2");

        assertEquals("classpath:test.p12", props.getKeystorePath());
        assertEquals("pass1", props.getKeystorePassword());
        assertEquals("classpath:trust.p12", props.getTruststorePath());
        assertEquals("pass2", props.getTruststorePassword());
    }
}
