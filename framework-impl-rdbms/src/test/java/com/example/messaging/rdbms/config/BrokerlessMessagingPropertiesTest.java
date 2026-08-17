package com.example.messaging.rdbms.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BrokerlessMessagingPropertiesTest {

    @Test
    public void testGettersAndSetters() {
        BrokerlessMessagingProperties props = new BrokerlessMessagingProperties();

        props.setEnabled(false);
        props.setPollIntervalMs(5000);

        assertFalse(props.isEnabled());
        assertEquals(5000, props.getPollIntervalMs());
        assertNotNull(props.getMtls());

        BrokerlessMessagingProperties.Mtls mtls = props.getMtls();
        mtls.setKeystorePath("classpath:test.p12");
        mtls.setKeystorePassword("pass1");
        mtls.setTruststorePath("classpath:trust.p12");
        mtls.setTruststorePassword("pass2");

        assertEquals("classpath:test.p12", mtls.getKeystorePath());
        assertEquals("pass1", mtls.getKeystorePassword());
        assertEquals("classpath:trust.p12", mtls.getTruststorePath());
        assertEquals("pass2", mtls.getTruststorePassword());
    }
}
