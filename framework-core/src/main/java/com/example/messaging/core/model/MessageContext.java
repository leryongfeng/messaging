package com.example.messaging.core.model;

import java.util.Map;

public class MessageContext {
    private String messageId;
    private String payload;
    private String messageType;
    private String aggregateId;
    private Long aggregateVersion;
    private Long sequenceNumber;
    private Map<String, String> headers;

    public MessageContext() {
    }

    public MessageContext(String messageId, String payload, String messageType, String aggregateId,
                          Long aggregateVersion, Long sequenceNumber, Map<String, String> headers) {
        this.messageId = messageId;
        this.payload = payload;
        this.messageType = messageType;
        this.aggregateId = aggregateId;
        this.aggregateVersion = aggregateVersion;
        this.sequenceNumber = sequenceNumber;
        this.headers = headers;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    public Long getAggregateVersion() {
        return aggregateVersion;
    }

    public void setAggregateVersion(Long aggregateVersion) {
        this.aggregateVersion = aggregateVersion;
    }

    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageContext that = (MessageContext) o;

        if (messageId != null ? !messageId.equals(that.messageId) : that.messageId != null) return false;
        if (payload != null ? !payload.equals(that.payload) : that.payload != null) return false;
        if (messageType != null ? !messageType.equals(that.messageType) : that.messageType != null) return false;
        if (aggregateId != null ? !aggregateId.equals(that.aggregateId) : that.aggregateId != null) return false;
        if (aggregateVersion != null ? !aggregateVersion.equals(that.aggregateVersion) : that.aggregateVersion != null) return false;
        if (sequenceNumber != null ? !sequenceNumber.equals(that.sequenceNumber) : that.sequenceNumber != null) return false;
        return headers != null ? headers.equals(that.headers) : that.headers == null;
    }

    @Override
    public int hashCode() {
        int result = messageId != null ? messageId.hashCode() : 0;
        result = 31 * result + (payload != null ? payload.hashCode() : 0);
        result = 31 * result + (messageType != null ? messageType.hashCode() : 0);
        result = 31 * result + (aggregateId != null ? aggregateId.hashCode() : 0);
        result = 31 * result + (aggregateVersion != null ? aggregateVersion.hashCode() : 0);
        result = 31 * result + (sequenceNumber != null ? sequenceNumber.hashCode() : 0);
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MessageContext{" +
                "messageId=\"" + messageId + "\"" +
                ", payload=\"" + payload + "\"" +
                ", messageType=\"" + messageType + "\"" +
                ", aggregateId=\"" + aggregateId + "\"" +
                ", aggregateVersion=" + aggregateVersion +
                ", sequenceNumber=" + sequenceNumber +
                ", headers=" + headers +
                "}";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String messageId;
        private String payload;
        private String messageType;
        private String aggregateId;
        private Long aggregateVersion;
        private Long sequenceNumber;
        private Map<String, String> headers;

        public Builder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public Builder messageType(String messageType) {
            this.messageType = messageType;
            return this;
        }

        public Builder aggregateId(String aggregateId) {
            this.aggregateId = aggregateId;
            return this;
        }

        public Builder aggregateVersion(Long aggregateVersion) {
            this.aggregateVersion = aggregateVersion;
            return this;
        }

        public Builder sequenceNumber(Long sequenceNumber) {
            this.sequenceNumber = sequenceNumber;
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public MessageContext build() {
            return new MessageContext(messageId, payload, messageType, aggregateId, aggregateVersion, sequenceNumber, headers);
        }
    }
}
