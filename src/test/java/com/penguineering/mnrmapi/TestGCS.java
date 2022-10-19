package com.penguineering.mnrmapi;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static io.micronaut.http.HttpHeaders.ACCEPT;
import static io.micronaut.http.HttpHeaders.AUTHORIZATION;
import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
public class TestGCS {

    @Inject
    protected HttpClient httpClient;  // always the same because the mock is defined as a singleton

    @Inject
    protected Authentication auth;

    @Inject
    protected GCS gcs;

    @Test
    public void testLookup() {
        final MockHttpClient httpClient = (MockHttpClient) this.httpClient;
        httpClient.reset();
        TestAuthentication.expireAuth(auth);

        final String testPath = "gcs-test-path";

        // verify the resulting URI
        StepVerifier.create(
                        gcs.lookup(Flux.just(testPath)))
                .expectNext(MockHttpClient.GCS_MOCK_RETRIEVAL_URI)
                .verifyComplete();

        // there should be two HTTP calls (auth and lookup)
        assertEquals(2, httpClient.requests.size());
        // auth call is tested in TestAuthentication, ignore
        httpClient.requests.remove(0);
        httpClient.assertRequest(HttpRequest
                .POST(Discovery.API_DOWNLOAD, GCS.FileRequestBody.withGet(testPath))
                .header(ACCEPT, "application/json")
                .header(AUTHORIZATION, "Bearer dummy"));
    }

    @Test
    public void testRetrieve() {
        final MockHttpClient httpClient = (MockHttpClient) this.httpClient;
        httpClient.reset();
        TestAuthentication.expireAuth(auth);

        final String testPath = "gcs-test-path";

        // test the actual retrieval result from Mock
        StepVerifier.create(
                        gcs.retrieve(Flux.just(testPath)))
                .expectNext("test")
                .verifyComplete();

        // there should be three HTTP calls (auth, lookup and retrieve)
        assertEquals(3, httpClient.requests.size());
        // auth call is tested in TestAuthentication, ignore
        httpClient.requests.remove(0);
        // lookup is tested above
        httpClient.requests.remove(0);
        httpClient.assertRequest(HttpRequest
                .GET(MockHttpClient.GCS_MOCK_RETRIEVAL_URI));
    }
}
