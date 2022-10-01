package com.penguineering.mnrmapi.notifications;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {
    final String messageId;
    private final Attributes attributes;


    public Message(@JsonProperty("messageid") String messageId,
                   @JsonProperty("attributes") Attributes attributes) {
        this.messageId = messageId;
        this.attributes = attributes;
    }

    @JsonProperty("messageid")
    public String getMessageId() {
        return messageId;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageId='" + messageId + '\'' +
                ", attributes=" + attributes +
                '}';
    }
}
