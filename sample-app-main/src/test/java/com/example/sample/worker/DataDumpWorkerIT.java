package com.example.sample.worker;

import com.example.messaging.core.model.MessageContext;
import com.example.messaging.core.spi.DeliveryMode;
import com.example.messaging.core.spi.MessagePublisher;
import com.example.sample.entity.ExportRecordEntity;
import com.example.sample.repository.ExportRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DataJpaTest
@Import(ObjectMapper.class)
public class DataDumpWorkerIT {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ExportRecordRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testDataDumpWorkerStreamsAllRecordsToCsv() throws Exception {
        // Given
        int totalRecords = 5000;
        for (int i = 1; i <= totalRecords; i++) {
            ExportRecordEntity entity = new ExportRecordEntity("Record " + i, "Category " + (i % 5), i * 1.5);
            entityManager.persist(entity);
            if (i % 1000 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush();

        MessagePublisher mockPublisher = mock(MessagePublisher.class);
        DataDumpWorker worker = new DataDumpWorker(repository, mockPublisher, objectMapper);

        String messageId = "msg-123456";
        Map<String, String> headers = new HashMap<>();
        headers.put("messageId", messageId);
        
        MessageContext context = MessageContext.builder()
                .messageType("DATA_DUMP")
                .payload("{\"exportName\":\"Full Extract\"}")
                .headers(headers)
                .build();

        // When
        worker.handle(context);

        // Then
        File csvFile = new File("/tmp/export_" + messageId + ".csv");
        assertThat(csvFile).exists();

        int lineCount = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineCount++;
            }
        }
        assertThat(lineCount).isEqualTo(totalRecords + 1);

        csvFile.delete();

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockPublisher, times(1)).publish(
                eq("DUMP_COMPLETED_WEBHOOK"),
                payloadCaptor.capture(),
                eq(DeliveryMode.AT_LEAST_ONCE)
        );

        String capturedPayload = payloadCaptor.getValue();
        assertThat(capturedPayload).contains(csvFile.getAbsolutePath());
    }
}
