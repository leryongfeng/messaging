package com.example.messaging.rdbms.repository;

import com.example.messaging.rdbms.entity.AbstractOutboxEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "test_audit_outbox")
public class AuditOutboxEntity extends AbstractOutboxEntity {
}
