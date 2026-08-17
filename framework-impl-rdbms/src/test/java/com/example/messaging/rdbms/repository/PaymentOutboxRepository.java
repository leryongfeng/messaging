package com.example.messaging.rdbms.repository;

import org.springframework.stereotype.Repository;

@Repository
public interface PaymentOutboxRepository extends BaseOutboxRepository<PaymentOutboxEntity> {
}
