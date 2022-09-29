package com.penguineering.mnrmapi;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties(RmApiConfig.PREFIX)
public class RmApiConfig {
    public static final String PREFIX = "rmapi";

    private String devicetoken;

    public RmApiConfig() {
    }

    protected void setDevicetoken(String devicetoken) {
        this.devicetoken = devicetoken;
    }

    public String getDevicetoken() {
        return devicetoken;
    }
}
