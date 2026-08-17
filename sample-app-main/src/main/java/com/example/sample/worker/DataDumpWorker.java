package com.example.sample.worker;

import com.example.messaging.core.model.MessageContext;
import com.example.messaging.core.spi.DeliveryMode;
import com.example.messaging.core.spi.MessagePublisher;
import com.example.messaging.rdbms.handler.AbstractEventHandler;
import com.example.sample.entity.ExportRecordEntity;
import com.example.sample.repository.ExportRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.PrintWriter;
import java.util.stream.Stream;

@Component
public class DataDumpWorker extends AbstractEventHandler<DataDumpRequestPayload> {

    private final ExportRecordRepository repository;
    private final MessagePublisher publisher;

    public DataDumpWorker(ExportRecordRepository repository,
                          MessagePublisher publisher,
                          ObjectMapper objectMapper) {
        super(objectMapper);
        this.repository = repository;
        this.publisher = publisher;
    }

    @Override
    public boolean supports(String messageType) {
        return "DATA_DUMP".equals(messageType);
    }

    @Override
    @Transactional(readOnly = true)
    public void process(DataDumpRequestPayload payload, MessageContext context) throws Exception {
        String messageId = context.getHeaders() != null ? context.getHeaders().get("messageId") : null;
        if (messageId == null) {
            messageId = java.util.UUID.randomUUID().toString();
        }

        File tempFile = new File("/tmp/export_" + messageId + ".csv");
        
        File parent = tempFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (Stream<ExportRecordEntity> entityStream = repository.streamAllForExport();
             PrintWriter writer = new PrintWriter(tempFile)) {

            writer.println("ID,Name,Category,RecordValue");

            entityStream.forEach(entity -> {
                writer.println(entity.getId() + "," +
                               escapeCsv(entity.getName()) + "," +
                               escapeCsv(entity.getCategory()) + "," +
                               entity.getRecordValue());
            });
        }

        publisher.publish(
                "DUMP_COMPLETED_WEBHOOK",
                "{\"filePath\":\"" + tempFile.getAbsolutePath() + "\"}",
                DeliveryMode.AT_LEAST_ONCE
        );
    }

    private String escapeCsv(String val) {
        if (val == null) {
            return "";
        }
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }
}
