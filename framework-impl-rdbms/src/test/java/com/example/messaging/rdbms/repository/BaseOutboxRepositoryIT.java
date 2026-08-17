package com.example.messaging.rdbms.repository;

import com.example.messaging.rdbms.entity.OutboxStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class BaseOutboxRepositoryIT {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AuditOutboxRepository auditOutboxRepository;

    @Autowired
    private PaymentOutboxRepository paymentOutboxRepository;

    @Autowired
    private List<BaseOutboxRepository<?>> allRepositories;

    @Test
    void testPollPendingMessagesFromMultipleTables() {
        // Given: Records in both audit and payment outbox tables
        AuditOutboxEntity auditMessage = new AuditOutboxEntity();
        auditMessage.setMessageType("AUDIT_EVENT");
        auditMessage.setPayload("{\"user\":\"test-user\"}");
        auditMessage.setStatus(OutboxStatus.PENDING);
        auditMessage.setNextRetryAt(Instant.now());
        entityManager.persist(auditMessage);

        PaymentOutboxEntity paymentMessage = new PaymentOutboxEntity();
        paymentMessage.setMessageType("PAYMENT_DUE");
        paymentMessage.setPayload("{\"amount\":100.00}");
        paymentMessage.setStatus(OutboxStatus.PENDING);
        paymentMessage.setNextRetryAt(Instant.now());
        entityManager.persist(paymentMessage);

        entityManager.flush();

        // When: Polling each repository
        List<AuditOutboxEntity> polledAuditMessages = auditOutboxRepository.pollPendingMessages(Instant.now(), PageRequest.of(0, 10));
        List<PaymentOutboxEntity> polledPaymentMessages = paymentOutboxRepository.pollPendingMessages(Instant.now(), PageRequest.of(0, 10));

        // Then: Assert each repository polled its own record
        assertThat(polledAuditMessages).hasSize(1);
        assertThat(polledAuditMessages.get(0).getMessageType()).isEqualTo("AUDIT_EVENT");

        assertThat(polledPaymentMessages).hasSize(1);
        assertThat(polledPaymentMessages.get(0).getMessageType()).isEqualTo("PAYMENT_DUE");

        // And when: Polling via the injected list
        long totalPolled = allRepositories.stream()
            .mapToLong(repo -> repo.pollPendingMessages(Instant.now(), PageRequest.of(0, 10)).size())
            .sum();

        // Then: Assert both records are accounted for
        assertThat(allRepositories).hasSize(2);
    }
}
