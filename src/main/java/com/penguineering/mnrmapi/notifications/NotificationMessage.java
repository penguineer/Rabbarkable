package com.penguineering.mnrmapi.notifications;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.w3c.dom.Attr;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationMessage {

    private final Message message;

    public NotificationMessage(@JsonProperty("message") Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "NotificationMessage{" +
                "message=" + message +
                '}';
    }
}
