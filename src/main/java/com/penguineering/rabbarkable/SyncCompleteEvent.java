package com.penguineering.rabbarkable;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;

import java.time.Instant;

/**
 * Signifies the end of a synchronization process.
 *  *
 * @param timestamp When the change was registered
 * @param root The new root GCS
 * @param deviceId Which device created the change
 */

@Introspected
public record SyncCompleteEvent(
        @JsonProperty("timestamp")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant timestamp,

        @JsonProperty("root")
        String root,

        @JsonProperty("device-id")
        String deviceId) {
    public static final String TYPE = "sync complete";
    public static SyncCompleteEvent withValues(Instant timestamp, String root, String deviceId) {
        return new SyncCompleteEvent(timestamp, root, deviceId);
    }

    public SyncCompleteEvent(
            Instant timestamp,
            String root,
            String deviceId) {
        this.timestamp = timestamp;
        this.root = root;
        this.deviceId = deviceId;
    }

    @JsonProperty("type")
    public String getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return "RootChangeEvent{" +
                "timestamp=" + timestamp +
                ", root='" + root + '\'' +
                ", deviceId='" + deviceId + '\'' +
                '}';
    }
}
