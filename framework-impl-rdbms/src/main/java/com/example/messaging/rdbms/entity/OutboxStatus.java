package com.example.messaging.rdbms.entity;

public enum OutboxStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    SUPERSEDED
}
