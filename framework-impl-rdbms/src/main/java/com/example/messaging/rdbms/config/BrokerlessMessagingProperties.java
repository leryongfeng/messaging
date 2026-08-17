package com.example.messaging.rdbms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "brokerless.messaging")
public class BrokerlessMessagingProperties {
    private boolean enabled = true;
    private long pollIntervalMs = 2000;
    private final Mtls mtls = new Mtls();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public long getPollIntervalMs() { return pollIntervalMs; }
    public void setPollIntervalMs(long pollIntervalMs) { this.pollIntervalMs = pollIntervalMs; }

    public Mtls getMtls() { return mtls; }

    public static class Mtls {
        private String keystorePath;
        private String keystorePassword;
        private String truststorePath;
        private String truststorePassword;

        public String getKeystorePath() { return keystorePath; }
        public void setKeystorePath(String keystorePath) { this.keystorePath = keystorePath; }

        public String getKeystorePassword() { return keystorePassword; }
        public void setKeystorePassword(String keystorePassword) { this.keystorePassword = keystorePassword; }

        public String getTruststorePath() { return truststorePath; }
        public void setTruststorePath(String truststorePath) { this.truststorePath = truststorePath; }

        public String getTruststorePassword() { return truststorePassword; }
        public void setTruststorePassword(String truststorePassword) { this.truststorePassword = truststorePassword; }
    }
}
