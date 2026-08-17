package com.example.messaging.core.exception;

public class RetriableDeliveryException extends RuntimeException {
    public RetriableDeliveryException(String message) {
        super(message);
    }

    public RetriableDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
