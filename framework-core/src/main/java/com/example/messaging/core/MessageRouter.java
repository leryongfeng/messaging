package com.example.messaging.core;

import com.example.messaging.core.model.MessageContext;
import com.example.messaging.core.spi.EventHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MessageRouter {

    private final List<EventHandler> handlers;

    public MessageRouter(List<EventHandler> handlers) {
        this.handlers = handlers;
    }

    public void route(MessageContext context) throws Exception {
        if (context == null || context.getMessageType() == null) {
            throw new IllegalArgumentException("Message context or message type cannot be null");
        }

        boolean handled = false;
        for (EventHandler handler : handlers) {
            if (handler.supports(context.getMessageType())) {
                handler.handle(context);
                handled = true;
                break;
            }
        }

        if (!handled) {
            throw new IllegalArgumentException("No EventHandler registered for message type: " + context.getMessageType());
        }
    }
}
