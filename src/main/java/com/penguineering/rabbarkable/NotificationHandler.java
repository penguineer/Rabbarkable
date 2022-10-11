package com.penguineering.rabbarkable;

import com.penguineering.mnrmapi.index.IndexAccess;
import com.penguineering.mnrmapi.notifications.NotificationAccess;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Context;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuples;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Instant;

/**
 * Listen to events from the reMarkable API and send RabbitMQ events accordingly.
 */
@Bean
@Singleton
@Context
public class NotificationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationHandler.class);

    @Inject
    NotificationAccess notifications;

    @Inject
    IndexAccess indexAccess;

    @Inject
    EventEmitter emitter;

    private Disposable subscription = null;

    @PostConstruct
    protected void init() {
        subscription = notifications.getMessageFlux()
                // store the current timestamp (do this before async tasks for precision)
                .timestamp()
                // store the new root GCS
                .flatMap(t -> Flux.just(t).zipWith(indexAccess.retrieveRootGcs()))
                // untangle tuples to: timestamp, root, notification message
                .map(t -> Tuples.of(t.getT1().getT1(), t.getT2(), t.getT1().getT2()))
                // create the root change event, including necessary mappings
                .map(t -> SyncCompleteEvent.withValues(
                        Instant.ofEpochMilli(t.getT1()),
                        t.getT2(),
                        t.getT3().getMessage().getAttributes().getSourceDeviceID()))
                // send to RabbitMQ
                .subscribe(evt -> EventEmitter.emitSyncCompleteEvent(emitter, evt));

        LOGGER.info("Subscribed to reMarkable events for RabbitMQ emission.");
    }

    @PreDestroy
    protected void destroy() {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
            LOGGER.info("Disposed subscription for reMarkable events.");
        }
    }
}
