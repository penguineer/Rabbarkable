package com.penguineering.mnrmapi.notifications;

import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.*;
import io.micronaut.websocket.exceptions.WebSocketSessionException;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;

@ClientWebSocket
public class NotificationClient implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationClient.class);

    /**
     * WebSocket connection times out after 300s; renew 30s before to be on the safe side.
     */
    private static final Duration HEARTBEAT_INTERVAL = Duration.ofSeconds(300-30);

    private WebSocketSession session;

    private Instant lastActivity;
    Disposable heartbeatSubscription = null;

    transient private Sinks.Many<NotificationMessage> messageSink = null;

    public synchronized void setMessageSink(Sinks.Many<NotificationMessage> messageSink) {
        this.messageSink = messageSink;
    }

    private void activity() {
        this.lastActivity = Instant.now();
    }

    @OnOpen
    public void onOpen(WebSocketSession session) {
        activity();

        this.session = session;

        if (this.heartbeatSubscription != null)
            this.heartbeatSubscription.dispose();
        this.heartbeatSubscription = this.createHeartbeatSubscription();

    }

    @OnMessage
    public void onMessage(NotificationMessage message) {
        activity();

        synchronized (this) {
            if (this.messageSink != null) {
                final Sinks.EmitResult er = this.messageSink.tryEmitNext(message);
                if (er != Sinks.EmitResult.OK)
                    LOGGER.warn("Tried to emit message with emit result {}: {}", er, message);
            } else
                LOGGER.warn("Message is discarded as target sink is not available: {}", message);
        }
    }

    @OnError
    public void onError(Throwable error) {
        synchronized (this) {
            if (this.messageSink != null) {
                final Sinks.EmitResult er = this.messageSink.tryEmitError(error);
                if (er != Sinks.EmitResult.OK)
                    LOGGER.warn("Tried to emit error with emit result {}: {}", er, error);
            } else
                LOGGER.warn("Error is discarded as target sink is not available:", error);
        }
        activity();
    }

    @OnClose
    @Override
    public void close() {
        Duration idle = Duration.between(this.lastActivity, Instant.now());
        LOGGER.info("Session closed after " + idle.toSeconds() + "s idle time.");

        if (heartbeatSubscription != null)
            heartbeatSubscription.dispose();

        if (this.session != null)
            session.close();
    }

    @Override
    public String toString() {
        return "NotificationClient{" +
                "object=" + Integer.toHexString(hashCode()) +
                ", session=" + session +
                '}';
    }

    public Publisher<Boolean> sendHeartbeat() {
        final String heartbeatMessage = "{}";

        return Mono.just(heartbeatMessage)
                .map(this::send)
                .flatMap(Mono::from)
                .onErrorReturn(e -> e instanceof WebSocketSessionException, "closed")
                .map(heartbeatMessage::equals);
    }

    public Publisher<String> send(String msg) {
        activity();
        return session.send(msg);
    }

    public boolean isConnected() {
        return session != null && session.isOpen();
    }

    private Disposable createHeartbeatSubscription() {
        return Flux.interval(HEARTBEAT_INTERVAL)
                .flatMap(i -> Mono.justOrEmpty(this))
                .filter(NotificationClient::isConnected)
                .flatMap(NotificationClient::sendHeartbeat)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(this::checkHeartbeat);
    }

    private void checkHeartbeat(boolean alive) {
        if (alive)
            LOGGER.info("Heartbeat was successful.");
        else {
            LOGGER.warn("Heartbeat was not successful! Renewing connection.");
            this.close();
        }
    }
}