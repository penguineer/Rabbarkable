package com.penguineering.mnrmapi;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.netty.DefaultHttpClient;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Bean
@Singleton  // this is a singleton for test verification
@Replaces(bean = DefaultHttpClient.class)
@Requires(env = {"test"})
public class MockHttpClient extends DefaultHttpClient {
    final List<HttpRequest<?>> requests = new CopyOnWriteArrayList<>();

    /**
     * Allow to set some argument to be evaluated for the next response
     */
    Object nextResponseArg = null;

    @SuppressWarnings("unchecked")
    public <I> void assertRequest(@NonNull io.micronaut.http.HttpRequest<I> expected) {
        assertTrue(requests.size() > 0);

        HttpRequest<?> req = requests.remove(0);

        assertEqualRequests(expected, (HttpRequest<I>) req);
    }

    public static <I> void assertEqualRequests(@NonNull io.micronaut.http.HttpRequest<I> expected,
                                               @NonNull io.micronaut.http.HttpRequest<I> actual) {
        assertEquals(expected.getUri(), actual.getUri());
        assertEquals(expected.getMethod(), actual.getMethod());
        assertEquals(expected.getHeaders().asMap(), actual.getHeaders().asMap());
        assertEquals(expected.getBody(), actual.getBody());
    }

    @Override
    public <I, O, E> Publisher<HttpResponse<O>> exchange(@NonNull io.micronaut.http.HttpRequest<I> request,
                                                         @NonNull Argument<O> bodyType,
                                                         @NonNull Argument<E> errorType) {
        requests.add(request);

        // Return dummy for user tokens
        if (request.getUri().equals(Discovery.USER_TOKEN_URI))
            return exchangeUserAuthentication();

        throw new RuntimeException("HTTP request not caught by mock: " + request);
    }

    @SuppressWarnings("unchecked")
    public <O> Publisher<HttpResponse<O>> exchangeUserAuthentication() {
        int rType = nextResponseArg == null ? 200 : (int) nextResponseArg;

        if (rType == 401)
            return Mono.just(HttpResponse.unauthorized());

        return Mono.just(HttpResponse.ok((O) RmApiTestConfig.dummyUserToken));
    }

}
