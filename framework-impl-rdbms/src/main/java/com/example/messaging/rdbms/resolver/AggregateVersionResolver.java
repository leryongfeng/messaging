package com.example.messaging.rdbms.resolver;

public interface AggregateVersionResolver {
    long getCurrentVersion(String aggregateId);
}
