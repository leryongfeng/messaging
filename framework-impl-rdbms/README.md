# Framework RDBMS Module

Implements outbox persistence using relational databases with JDBC event listeners and cluster-safe scheduled pollers.

## 1. Outbox Mechanisms
- **Mapped Entity**: The base outbox configuration uses `@MappedSuperclass` `AbstractOutboxEntity` which maps outbox database mappings to concrete entities.
- **SKIP LOCKED Polling**: Employs database concurrency features to safely partition and query rows without locking threads across multiple clusters.
- **Exponential Backoff**: Failed outbound webhook attempts are rescheduled using an exponential backoff algorithm (`2^retryCount` seconds delay).

## 2. Auto-Configuration Properties

Configure these properties in your application context (`application.yml` or `application.properties`):

| Property | Default | Description |
| :--- | :--- | :--- |
| `brokerless.messaging.enabled` | `true` | Toggles the outbox poller engine bean setup. |
| `brokerless.messaging.poll-interval-ms` | `2000` | Scheduled frequency (milliseconds) for querying the outbox database table. |
| `framework.messaging.webhook.mtls.keystore-path` | - | Location (classpath: or file path) for the outbound client keystore. |
| `framework.messaging.webhook.mtls.keystore-password` | - | Password for unlocking client keystore. |
| `framework.messaging.webhook.mtls.truststore-path` | - | Location for verifying external servers. |
| `framework.messaging.webhook.mtls.truststore-password` | - | Password for unlocking truststore. |

## 3. Logical Flow

```mermaid
flowchart TD
%% Dark Mode Optimized Styles
    classDef client fill:#0D47A1,stroke:#64B5F6,stroke-width:2px,color:#FFFFFF;
    classDef service fill:#E65100,stroke:#FFB74D,stroke-width:2px,color:#FFFFFF;
    classDef framework fill:#1B5E20,stroke:#81C784,stroke-width:2px,color:#FFFFFF;
    classDef database fill:#880E4F,stroke:#F06292,stroke-width:2px,color:#FFFFFF;
    classDef external fill:#4A148C,stroke:#BA68C8,stroke-width:2px,color:#FFFFFF;

%% Client / Trigger Layer
    subgraph Trigger["1. Trigger (Controller / Client)"]
        A([API Request: e.g., Update User]):::client
    end

%% Service Layer (Transaction Boundary 1)
    subgraph ServiceLayer["2. Service Layer (@Transactional)"]
        B{Execute Business Logic}:::service
        C[Save Domain Entity <br/> e.g., UserRecord]:::service
        D[Call MessagePublisher.publish]:::service
        E[Serialize Payload & Generate Idempotency-Key]:::framework
        F[Save Outbox Entity]:::framework
        G((Commit DB Transaction)):::service

        A --> B
        B --> C
        C --> D
        D --> E
        E --> F
        F --> G
    end

%% Database
    subgraph DB["3. Relational Database"]
        DB_T[(Domain Tables)]:::database
        DB_O[(Outbox Table)]:::database

        C -.-> DB_T
        F -.-> DB_O
    end

%% Sender / Poller Layer (Transaction Boundary 2)
    subgraph Sender["4. Sender (Framework Polling Engine)"]
        H([Scheduled Job Trigger]):::framework
        I{Find Pending Messages}:::framework
        J[Lock Rows: SKIP LOCKED]:::framework
        K[Message Router / Webhook Dispatcher]:::framework

        G -. "Asynchronous" .-> H
        H --> I
        I -. "SELECT ... FOR UPDATE" .-> DB_O
        I --> J
        J --> K
    end

%% Receiver Layer
    subgraph Receiver["5. Receiver (External System)"]
        L([Mock Receiver Endpoint]):::external
        M{Validate Idempotency-Key}:::external
        N[Process Business Event]:::external
        O((Return HTTP 200 OK)):::external

        L --> M
        M --> N
        N --> O
    end

%% Cleanup (Transaction Boundary 2 completes)
    subgraph Cleanup["6. Post-Delivery Cleanup"]
        P[Delete / Mark Outbox Row Processed]:::framework
        Q((Commit DB Transaction)):::framework

        P -.-> DB_O
        P --> Q
    end

%% Network Connections
    K -- "HTTP POST with mTLS & Headers" --> L
    O -- "Success Response" --> P
```