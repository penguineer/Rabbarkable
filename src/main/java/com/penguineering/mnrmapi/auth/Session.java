package com.penguineering.mnrmapi.auth;

import com.penguineering.mnrmapi.Paths;
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

import static io.micronaut.http.HttpHeaders.ACCEPT;
import static io.micronaut.http.HttpHeaders.AUTHORIZATION;

@Bean
@Singleton
public class Session {
    private static final Logger LOGGER = LoggerFactory.getLogger(Session.class);

    private final String deviceToken;

    @Inject
    protected HttpClient httpClient;

    private UserToken userToken; // Note that the token may be accessed in a multithreaded environment

    public Session(@NotNull RmApiConfig config) {
        this.deviceToken = config.getDevicetoken();
    }

    private synchronized void setUserToken(UserToken userToken) {
        this.userToken = userToken;
    }

    private synchronized UserToken getUserToken() {
        return userToken;
    }

    protected Mono<UserToken> renewUserToken() {
        return Mono
                .just(HttpRequest
                        .POST(Paths.USER_TOKEN_URI, null)
                        .header(ACCEPT, "text/plain")
                        .header(AUTHORIZATION, "Bearer " + deviceToken))
                .map(httpClient::retrieve)
                .flatMap(Mono::from)
                .switchIfEmpty(
                        Mono.error(
                                new Exception("Login returned empty response")))
                .map(UserToken::withToken)
                .doOnNext(this::setUserToken)
                .doOnNext(userToken ->
                        LOGGER.info("User token has been renewed and will expire at {}.", userToken.getExpires()));
    }

    protected Mono<String> validUserToken() {
        return Mono
                .justOrEmpty(this.getUserToken()) // use getter to synchronize
                .filter(UserToken::isValid)
                .switchIfEmpty(Mono.defer(this::renewUserToken))
                .map(UserToken::getToken);
    }

    public <B> Flux<MutableHttpRequest<B>> userAuthenticatedRequest(Flux<MutableHttpRequest<B>> request) {
        // There might be a race condition if parallel user tokens are not accepted. Need to observe that.
        // See https://github.com/penguineer/Rabbarkable/issues/3

        return request
                // Execute the Mono for each Flux element
                .flatMap(req -> Mono.just(req).zipWith(validUserToken()))
                .map(t -> t.getT1().header(AUTHORIZATION, "Bearer " + t.getT2()));
    }
}
