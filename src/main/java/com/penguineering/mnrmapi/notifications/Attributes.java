package com.penguineering.mnrmapi.notifications;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Attributes {
    final String auth0UserID;
    final String event;
    final String sourceDeviceID;

    public Attributes(@JsonProperty("auth0UserID") String auth0UserID,
                      @JsonProperty("event") String event,
                      @JsonProperty("sourceDeviceID") String sourceDeviceID) {
        this.auth0UserID = auth0UserID;
        this.event = event;
        this.sourceDeviceID = sourceDeviceID;
    }

    public String getAuth0UserID() {
        return auth0UserID;
    }

    public String getEvent() {
        return event;
    }

    public String getSourceDeviceID() {
        return sourceDeviceID;
    }

    @Override
    public String toString() {
        return "Attributes{" +
                "auth0UserID='" + auth0UserID + '\'' +
                ", event='" + event + '\'' +
                ", sourceDeviceID='" + sourceDeviceID + '\'' +
                '}';
    }
}
