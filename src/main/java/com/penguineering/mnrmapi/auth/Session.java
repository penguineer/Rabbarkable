package com.penguineering.mnrmapi.auth;

import com.penguineering.mnrmapi.Discovery;
import com.penguineering.mnrmapi.RmApiConfig;
import io.micronaut.context.annotation.Bean;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.HttpClient;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static io.micronaut.http.HttpHeaders.ACCEPT;
import static io.micronaut.http.HttpHeaders.AUTHORIZATION;

/**
 * Manage User Token subscriptions to the re:markable API.
 *
 * <p>User Tokens are retrieved per-session based on the Device Token.</p>
 *
 * <p>As User Tokens have a limited life-span, they are automatically renewed on expiry.</p>
 */
@Bean
@Singleton
public class Session {
    private static final Logger LOGGER = LoggerFactory.getLogger(Session.class);

    private final String deviceToken;

    @Inject
    protected HttpClient httpClient;

    @Inject
    protected Discovery discovery;

    transient private UserToken userToken; // Note that the token may be accessed in a multithreaded environment
    private final Lock userTokenLock = new ReentrantLock();

    public Session(@NotNull RmApiConfig config) {
        this.deviceToken = config.getDevicetoken();
    }

    protected Mono<UserToken> renewUserToken() {
        return discovery.fetchUserTokenURI()
                .map(uri -> HttpRequest.POST(uri, null)
                        .header(ACCEPT, "text/plain")
                        .header(AUTHORIZATION, "Bearer " + deviceToken))
                .map(httpClient::retrieve)
                .flatMap(Mono::from)
                .switchIfEmpty(
                        Mono.error(
                                new Exception("Login returned empty response")))
                .map(UserToken::withToken)
                .doOnNext(userToken ->
                        LOGGER.info("User token has been renewed and will expire at {}.", userToken.getExpires()));
    }

    protected Mono<String> validUserToken() {
        return Mono.fromCallable(() -> {
            userTokenLock.lock();
            try {
                if (userToken == null || !userToken.isValid())
                    // TODO is there a way to do this without blocking in a Callable?
                    userToken = renewUserToken().block();

                if (userToken == null || userToken.getToken() == null)
                    throw new IllegalStateException("User token is null");

                return userToken.getToken();
            } finally {
                userTokenLock.unlock();
            }
        });
    }

    /**
     * Add authentication headers with a User Token to a request
     *
     * <p>If the token is expired or not available a new token will be issued.</p>
     *
     * @param request HttpRequest to decorate
     * @return Decorated HttpRequest
     * @param <B> HttpRequest body type
     */
    public <B> Flux<MutableHttpRequest<B>> userAuthenticatedRequest(Flux<MutableHttpRequest<B>> request) {
        return request
                // Execute the Mono for each Flux element
                .flatMap(req -> Mono.just(req).zipWith(validUserToken()))
                .map(t -> t.getT1().header(AUTHORIZATION, "Bearer " + t.getT2()));
    }
}
