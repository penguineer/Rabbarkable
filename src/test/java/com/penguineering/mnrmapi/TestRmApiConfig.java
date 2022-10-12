package com.penguineering.mnrmapi;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class TestRmApiConfig {
    @Inject
    RmApiConfig config;

    @Test
    public void testDummyConfigAvailable() {
        // Make sure we got the dummy config via injection, as this is the test environment
        assertInstanceOf(RmApiTestConfig.class, config);

        assertEquals("dummy", config.getDevicetoken());
    }

    @Test
    public void testRmApiConfig() {
        final RmApiConfig cfg = new RmApiConfig();
        assertNull(cfg.getDevicetoken());

        cfg.setDevicetoken("dummy");
        assertEquals("dummy", cfg.getDevicetoken(), "Device token does not match setter call!");
    }

}
