package com.penguineering.mnrmapi.notifications;

import com.penguineering.mnrmapi.Authentication;
import com.penguineering.mnrmapi.Discovery;
import io.micronaut.context.annotation.Bean;
import io.micronaut.http.HttpRequest;
import io.micronaut.reactor.http.client.websocket.ReactorWebSocketClient;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Bean
@Singleton
public class NotificationAccess implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationAccess.class);

    @Inject
    Discovery discovery;

    @Inject
    Authentication auth;

    @Inject
    ReactorWebSocketClient webSocketClient;

    transient private NotificationClient notificationClient = null;
    private final Lock notificationClientLock = new ReentrantLock();

    private Disposable reconnectSubscription = null;

    private final Sinks.Many<NotificationMessage> messageSink = Sinks.many().multicast().onBackpressureBuffer();
    private final Sinks.Many<String> reconnectSink = Sinks.many().unicast().onBackpressureBuffer();

    @PostConstruct
    public void init() {
        // store the subscription so that re-connect attempts can be cancelled on shutdown
        reconnectSubscription = reconnectSink.asFlux()
                // Ignore subsequent requests if already busy
                .onBackpressureDrop()
                // Lock and request a new connection
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(c -> Flux.just(c).zipWith(Mono.fromCallable(() -> {
                            notificationClientLock.lock();
                            try {
                                return isConnected()
                                        ? notificationClient
                                        : this.connect().block();
                            } finally {
                                notificationClientLock.unlock();
                            }
                        })
                        .subscribeOn(Schedulers.boundedElastic())))
                .map(Tuple2::getT2)
                // The WebSocket implementation returns the client here,
                // which is called for any message. The Flux generated
                // in the setup will only return clients, but not actual
                // messages.
                .subscribe(this::updateNotificationClient);

        reconnectSink.tryEmitNext("initial connect");
    }

    public boolean isConnected() {
        notificationClientLock.lock();
        try {
            return this.notificationClient != null && this.notificationClient.isConnected();
        } finally {
            notificationClientLock.unlock();
        }
    }

    private void updateNotificationClient(NotificationClient notificationClient) {
        notificationClientLock.lock();

        try {

            if (notificationClient == this.notificationClient)
                return; // idempotent setter: do nothing if instances are identical

            if (this.notificationClient != null && this.notificationClient.isConnected())
                this.notificationClient.close();

            this.notificationClient = notificationClient;
            this.notificationClient.setMessageSink(this.messageSink);
            this.notificationClient.setReconnectSink(this.reconnectSink);
        } finally {
            notificationClientLock.unlock();
        }
    }

    private Mono<NotificationClient> connect() {
        return Flux.from(Mono.defer(() -> discovery.fetchNotificationURI()))
                .map(HttpRequest::GET)
                .transform(auth::userAuthenticatedRequest)
                .flatMap(req -> webSocketClient.connect(NotificationClient.class, req))
                .single()
                .doOnNext(cl -> LOGGER.info("New re:markable notification connection: {}", cl));
    }

    @PreDestroy
    @Override
    public void close() {
        if (reconnectSubscription != null) {
            LOGGER.info("Disposing reconnect-subscription for notification websocket.");
            reconnectSubscription.dispose();
        }

        // notificationClient is closed automatically
    }

    public Flux<NotificationMessage> getMessageFlux() {
        return messageSink.asFlux();
    }
}
