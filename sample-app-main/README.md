# Sample App Main

A demonstration application running the outbox messaging framework with local endpoints enabled for manual testing and verification.

## 1. Running the Application
Run the Spring Boot application using Maven:
```bash
mvn spring-boot:run -pl sample-app-main
```

## 2. Manual End-to-End Verification

Publish an outbound event:
```bash
curl -X POST "http://localhost:8080/test/send-webhook?messageType=OUTBOUND_WEBHOOK&targetUrl=http://localhost:8080/receiver/webhook" \
  -H "Content-Type: application/json" \
  -d '{"event": "manual-test", "description": "Verification payload"}'
```

### Expected Flow
1. **Send Webhook**: The `TestSenderController` receives the command parameters, puts `targetUrl` into the payload, and publishes it via `MessagePublisher`.
2. **Outbox Persisted**: Under the hood, the outbox persistence handles the event and inserts the record inside the persistent datastore.
3. **Dispatch & Reception**: The `OutboxPollingEngine` queries the outbox and routes `OUTBOUND_WEBHOOK` event to the `ExternalWebhookDispatcher`, which dispatches the POST request to the mock `TestReceiverController` (`/receiver/webhook`).
4. **Log Output**: Check application terminal logs to visually confirm the request headers and body payload:
```text
=== Received Webhook Block ===
Headers:
  Idempotency-Key   : msg-xxxx-xxxx-xxxx
  X-Aggregate-Id    : null
  X-Sequence-Number: null
Payload:
{"event":"manual-test","description":"Verification payload","targetUrl":"http://localhost:8080/receiver/webhook"}
=============================
```
