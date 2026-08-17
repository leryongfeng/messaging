package com.example.messaging.rdbms.repository;

import com.example.messaging.rdbms.entity.AbstractOutboxEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "test_payment_outbox")
public class PaymentOutboxEntity extends AbstractOutboxEntity {
}
