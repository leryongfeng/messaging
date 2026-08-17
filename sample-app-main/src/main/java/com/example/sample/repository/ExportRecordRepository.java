package com.example.sample.repository;

import com.example.sample.entity.ExportRecordEntity;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
public interface ExportRecordRepository extends JpaRepository<ExportRecordEntity, Long> {

    @QueryHints({
        @QueryHint(name = "org.hibernate.fetchSize", value = "1000"),
        @QueryHint(name = "org.hibernate.cacheable", value = "false"),
        @QueryHint(name = "org.hibernate.readOnly", value = "true")
    })
    @Query("SELECT e FROM ExportRecordEntity e")
    Stream<ExportRecordEntity> streamAllForExport();
}
