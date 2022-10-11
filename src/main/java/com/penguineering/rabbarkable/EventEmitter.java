package com.penguineering.rabbarkable;

import io.micronaut.messaging.annotation.MessageHeader;
import io.micronaut.rabbitmq.annotation.Binding;
import io.micronaut.rabbitmq.annotation.RabbitClient;
import io.micronaut.rabbitmq.annotation.RabbitProperty;

@RabbitClient
public interface EventEmitter {
    /**
     * Emit a sync complete event to AMQP.
     *
     * <p>This is a helper that makes sure to set the correct headers, as there are
     * redundancies between header and message content.</p>
     *
     * @param client The RabbitMQ client instance
     * @param evt The event to emit.
     */
    static void emitSyncCompleteEvent(EventEmitter client, SyncCompleteEvent evt) {
        client.syncComplete(evt, evt.deviceId());
    }

    @RabbitProperty(name = "contentType", value = "application/json")
    @MessageHeader(name = "x-type", value = SyncCompleteEvent.TYPE)
    @Binding("${rabbitmq.exchange}")
    void syncComplete(SyncCompleteEvent evt, @MessageHeader("x-device-id") String deviceId);
}
