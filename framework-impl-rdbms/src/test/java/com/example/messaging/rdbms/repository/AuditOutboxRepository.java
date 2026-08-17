package com.example.messaging.rdbms.repository;

import org.springframework.stereotype.Repository;

@Repository
public interface AuditOutboxRepository extends BaseOutboxRepository<AuditOutboxEntity> {
}
