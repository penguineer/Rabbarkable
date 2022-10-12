package com.penguineering.mnrmapi;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;

@ConfigurationProperties(RmApiConfig.PREFIX)
@Requires(notEnv = {"test"})
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
