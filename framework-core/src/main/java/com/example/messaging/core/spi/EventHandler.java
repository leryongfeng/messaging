package com.example.messaging.core.spi;

import com.example.messaging.core.model.MessageContext;

public interface EventHandler {
    boolean supports(String messageType);
    void handle(MessageContext context) throws Exception;
}
