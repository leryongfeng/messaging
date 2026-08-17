package com.example.sample.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "test_export_record")
public class ExportRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;
    private String category;
    private double recordValue;

    public ExportRecordEntity() {}

    public ExportRecordEntity(String name, String category, double recordValue) {
        this.name = name;
        this.category = category;
        this.recordValue = recordValue;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getRecordValue() { return recordValue; }
    public void setRecordValue(double recordValue) { this.recordValue = recordValue; }
}
