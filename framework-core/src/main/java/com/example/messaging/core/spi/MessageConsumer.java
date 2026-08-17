package com.example.messaging.core.spi;

import com.example.messaging.core.MessageRouter;

public interface MessageConsumer {
    void startConsuming(MessageRouter router);
    void acknowledge(String messageId);
    void markFailed(String messageId, Exception e);
}
