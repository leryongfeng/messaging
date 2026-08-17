package com.example.messaging.core.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.time.Duration;

@Configuration
public class WebhookMtlsConfig {

    private final WebhookMtlsProperties properties;

    public WebhookMtlsConfig(WebhookMtlsProperties properties) {
        this.properties = properties;
    }

    @Bean
    public WebClient mtlsWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(5));

        try {
            if (properties.getKeystorePath() != null && !properties.getKeystorePath().isBlank()) {
                KeyStore keyStore = KeyStore.getInstance("PKCS12");
                try (InputStream ksStream = getResourceStream(properties.getKeystorePath())) {
                    keyStore.load(ksStream, properties.getKeystorePassword().toCharArray());
                }

                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(keyStore, properties.getKeystorePassword().toCharArray());

                KeyStore trustStore = KeyStore.getInstance("PKCS12");
                try (InputStream tsStream = getResourceStream(properties.getTruststorePath())) {
                    trustStore.load(tsStream, properties.getTruststorePassword().toCharArray());
                }

                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(trustStore);

                SslContext sslContext = SslContextBuilder.forClient()
                        .keyManager(kmf)
                        .trustManager(tmf)
                        .build();

                httpClient = httpClient.secure(sslSpec -> sslSpec.sslContext(sslContext));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to configure mTLS WebClient", e);
        }

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    private InputStream getResourceStream(String path) throws Exception {
        if (path.startsWith("classpath:")) {
            String classPath = path.substring("classpath:".length());
            InputStream stream = getClass().getClassLoader().getResourceAsStream(classPath);
            if (stream == null) {
                throw new IllegalArgumentException("Resource not found: " + path);
            }
            return stream;
        }
        return Files.newInputStream(Paths.get(path));
    }
}
