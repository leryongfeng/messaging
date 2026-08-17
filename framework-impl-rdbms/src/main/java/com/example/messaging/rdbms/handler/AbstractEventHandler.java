package com.example.messaging.rdbms.handler;

import com.example.messaging.core.model.MessageContext;
import com.example.messaging.core.spi.EventHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.ParameterizedType;

public abstract class AbstractEventHandler<T> implements EventHandler {

    private final Class<T> payloadType;
    private final ObjectMapper objectMapper;

    @SuppressWarnings("unchecked")
    protected AbstractEventHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.payloadType = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
    }

    @Override
    public void handle(MessageContext context) throws Exception {
        T payload = objectMapper.readValue(context.getPayload(), payloadType);
        process(payload, context);
    }

    protected abstract void process(T payload, MessageContext context) throws Exception;
}
