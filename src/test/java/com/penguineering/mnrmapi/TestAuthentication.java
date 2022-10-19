package com.penguineering.mnrmapi;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.lang.reflect.Field;
import java.net.URI;
import java.time.Instant;

import static io.micronaut.http.HttpHeaders.ACCEPT;
import static io.micronaut.http.HttpHeaders.AUTHORIZATION;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
public class TestAuthentication {
    @Inject
    protected HttpClient httpClient;  // always the same because the mock is defined as a singleton

    @Inject
    protected Authentication auth;

    @Test
    public void testSuccessfullyAuthenticatedRequest() {
        final MockHttpClient httpClient = (MockHttpClient) this.httpClient;
        httpClient.nextResponseArg = null;

        // do the request ("first time")
        requestWithNewUserToken(createRequest(), createDecoratedRequest());

        // run a second time
        StepVerifier.create(
                        auth.userAuthenticatedRequest(Flux.just(createRequest())))
                .consumeNextWith(decoratedReq ->
                        MockHttpClient.assertEqualRequests(
                                createDecoratedRequest(),
                                decoratedReq))
                .verifyComplete();

        // no further user auth requests should have happened
        assertEquals(0, httpClient.requests.size());

        expireAuth(auth);

        // do it again, now it should be like the first time,
        // i.e. the token is renewed
        requestWithNewUserToken(createRequest(), createDecoratedRequest());
    }

    private void requestWithNewUserToken(MutableHttpRequest<String> req, MutableHttpRequest<String> decorated) {
        final MockHttpClient httpClient = (MockHttpClient) this.httpClient;

        // verify this is happening
        StepVerifier.create(
                        auth.userAuthenticatedRequest(Flux.just(req)))
                .consumeNextWith(decoratedReq ->
                        MockHttpClient.assertEqualRequests(decorated, decoratedReq))
                .verifyComplete();

        assertEquals(1, httpClient.requests.size());
        httpClient.assertRequest(
                HttpRequest.POST(Discovery.USER_TOKEN_URI, null)
                        .header(ACCEPT, "text/plain")
                        .header(AUTHORIZATION, "Bearer dummy"));
    }

    private MutableHttpRequest<String> createRequest() {
        return HttpRequest.GET(URI.create("https://www.example.com/"));
    }

    private MutableHttpRequest<String> createDecoratedRequest() {
        return HttpRequest.<String>GET(URI.create("https://www.example.com/"))
                .header(AUTHORIZATION, "Bearer " + RmApiTestConfig.dummyUserToken);
    }

    public static void expireAuth(Authentication auth) {
        if (auth == null)
            return;

        // expire the user token
        assertDoesNotThrow(() -> {
            // get the user token field (hidden in auth)
            Field userTokenField = Authentication.class.getDeclaredField("userToken");
            userTokenField.setAccessible(true);
            Object userToken = userTokenField.get(auth);

            if (userToken != null) {
                // get the UserToken class (hidden in Authentication)
                Class<?> userTokenClass = Class.forName(userTokenField.getGenericType().getTypeName());
                // get the "expires" field (hidden in UserToken)
                Field expiresField = userTokenClass.getDeclaredField("expires");
                expiresField.setAccessible(true);
                // user token now expired 10 seconds ago
                expiresField.set(userToken, Instant.now().minusSeconds(10));
            }
        });
    }

}
