package com.example.messaging.rdbms.repository;

import com.example.messaging.rdbms.entity.AbstractOutboxEntity;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@NoRepositoryBean
public interface BaseOutboxRepository<T extends AbstractOutboxEntity> extends JpaRepository<T, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")})
    @Query("SELECT m FROM #{#entityName} m WHERE m.status = com.example.messaging.rdbms.entity.OutboxStatus.PENDING AND m.nextRetryAt <= :now ORDER BY m.nextRetryAt ASC")
    List<T> pollPendingMessages(@Param("now") Instant now, Pageable pageable);

}
