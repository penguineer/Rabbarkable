package com.penguineering.mnrmapi;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;

@Bean
@Replaces(bean= com.penguineering.mnrmapi.RmApiConfig.class)
@Requires(env = {"test"})
public class RmApiTestConfig extends RmApiConfig {
    public static String dummyUserToken = "dummy";

    RmApiTestConfig() {
        super();
        super.setDevicetoken(dummyUserToken);
    }
}
