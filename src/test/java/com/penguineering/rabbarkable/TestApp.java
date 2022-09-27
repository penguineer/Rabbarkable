package com.penguineering.rabbarkable;

import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class TestApp {
    @Inject
    EmbeddedApplication<?> application;

    @Test
    void testAppRunning() {
        assertTrue(application.isRunning());
    }
}
