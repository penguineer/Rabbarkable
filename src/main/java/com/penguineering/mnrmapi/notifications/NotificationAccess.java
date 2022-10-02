package com.penguineering.mnrmapi.notifications;

import com.penguineering.mnrmapi.auth.Session;
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
import java.time.Duration;

@Bean
@Singleton
public class NotificationAccess implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationAccess.class);

    /**
     * How often to check if the connection needs to be renewed
     */
    private static final Duration CONN_CHECK_INTERVAL = Duration.ofSeconds(1);

    @Inject
    Discovery discovery;

    @Inject
    Session session;

    @Inject
    ReactorWebSocketClient webSocketClient;

    transient private NotificationClient notificationClient = null;

    private Disposable reconnectSubscription = null;

    private final Sinks.Many<NotificationMessage> messageSink = Sinks.many().multicast().onBackpressureBuffer();

    @PostConstruct
    public void init() {
        // store the subscription so that re-connect attempts can be cancelled on shutdown
        reconnectSubscription = Flux
                // only emits if there is no connection
                .interval(CONN_CHECK_INTERVAL)
                .filter(this::needsConnection)
                // Check periodically if a new connection is needed and
                // store established connections in `this.notificationClient`.
                .flatMap(c -> Flux.just(c).zipWith(Mono.defer(this::connect)))
                .map(Tuple2::getT2)
                // The WebSocket implementation returns the client here,
                // which is called for any message. The Flux generated
                // in the setup will only return clients, but not actual
                // messages.
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(this::setNotificationClient);
    }

    /**
     * @param _attempt running number of connection check
     * @return true if a new connection is needed
     */
    private synchronized boolean needsConnection(long _attempt) {
        return !isConnected();
    }

    public synchronized boolean isConnected() {
        return this.notificationClient != null && this.notificationClient.isConnected();
    }

    private synchronized void setNotificationClient(NotificationClient notificationClient) {
        if (this.notificationClient != null && this.notificationClient.isConnected())
            this.notificationClient.close();

        this.notificationClient = notificationClient;
        this.notificationClient.setMessageSink(this.messageSink);
    }

    private Mono<NotificationClient> connect() {
        return Flux.from(Mono.defer(() -> discovery.fetchNotificationURI()))
                .map(HttpRequest::GET)
                .transform(session::userAuthenticatedRequest)
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
