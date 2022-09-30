package com.penguineering.mnrmapi.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ServiceDiscoveryResult {
    private final String status;
    private final String host;

    ServiceDiscoveryResult(@JsonProperty(value="Status") String status,
                           @JsonProperty(value="Host") String host) {
        this.status = status;
        this.host = host;
    }

    public String getStatus() {
        return status;
    }

    public String getHost() {
        return host;
    }
}
