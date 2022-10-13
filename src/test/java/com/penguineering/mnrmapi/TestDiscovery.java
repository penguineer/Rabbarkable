package com.penguineering.mnrmapi;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class TestDiscovery {
    @Inject
    protected HttpClient httpClient;  // always the same because the mock is defined as a singleton

    @Inject
    protected Discovery discovery;

    @Test
    public void testFetchUserTokenURI() {
        final MockHttpClient httpClient = (MockHttpClient) this.httpClient;
        httpClient.reset();

        // verify the resulting URI
        StepVerifier.create(
                        discovery.fetchUserTokenURI())
                .expectNext(Discovery.USER_TOKEN_URI)
                .verifyComplete();

        // verify that no HTTP calls have been made
        assertEquals(0, httpClient.requests.size());
    }


    // Note that the path is fixed!
    static URI MOCK_NOTIFY_URI = URI.create("wss://www.example.com/notifications/ws/json/1");

    @Test
    public void testFetchNotificationURI() {
        final MockHttpClient httpClient = (MockHttpClient) this.httpClient;
        httpClient.reset();

        // verify the resulting URI with our mock URI
        StepVerifier.create(
                        discovery.fetchNotificationURI())
                .expectNext(MOCK_NOTIFY_URI)
                .verifyComplete();

        // verify the HTTP call
        assertEquals(1, httpClient.requests.size());
        httpClient.assertRequest(HttpRequest.GET(Discovery.NOTIFICATION_DISCOVERY_URI));
    }

    @Test
    public void testFetchDownloadURI() {
        final MockHttpClient httpClient = (MockHttpClient) this.httpClient;
        httpClient.reset();

        // verify the resulting URI
        StepVerifier.create(
                        discovery.fetchDownloadURI())
                .expectNext(Discovery.API_DOWNLOAD)
                .verifyComplete();

        // verify that no HTTP calls have been made
        assertEquals(0, httpClient.requests.size());
    }
}
