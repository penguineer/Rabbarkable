package com.penguineering.mnrmapi.document;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class Metadata {
    private final boolean deleted;
    private final Instant lastModified;
    private final Instant lastOpened;
    private final int lastOpenedPage;
    private final boolean metadatamodified;
    private final boolean modified;
    private final String parent;
    private final boolean pinned;
    private final boolean synced;
    private final String type;
    private final int version;
    private final String visibleName;

    static Instant instantFromString(String s) {
        if (s == null || s.isBlank())
            return null;
        return Instant.ofEpochMilli(Long.parseLong(s));
    }

    public Metadata(@JsonProperty(value="deleted") boolean deleted,
                    @JsonProperty(value="lastModified") String lastModified,
                    @JsonProperty(value="lastOpened") String lastOpened,
                    @JsonProperty(value="lastOpenedPage") int lastOpenedPage,
                    @JsonProperty(value="metadatamodified") boolean metadatamodified,
                    @JsonProperty(value="modified") boolean modified,
                    @JsonProperty(value="parent") String parent,
                    @JsonProperty(value="pinned") boolean pinned,
                    @JsonProperty(value="synced") boolean synced,
                    @JsonProperty(value="type") String type,
                    @JsonProperty(value="version") int version,
                    @JsonProperty(value="visibleName") String visibleName) {
        this.deleted = deleted;
        this.lastModified = instantFromString(lastModified);
        this.lastOpened = instantFromString(lastOpened);
        this.lastOpenedPage = lastOpenedPage;
        this.metadatamodified = metadatamodified;
        this.modified = modified;
        this.parent = parent;
        this.pinned = pinned;
        this.synced = synced;
        this.type = type; // DocumentType CollectionType
        this.version = version;
        this.visibleName = visibleName;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    public Instant getLastOpened() {
        return lastOpened;
    }

    public int getLastOpenedPage() {
        return lastOpenedPage;
    }

    public boolean isMetadatamodified() {
        return metadatamodified;
    }

    public boolean isModified() {
        return modified;
    }

    public String getParent() {
        return parent;
    }

    public boolean isPinned() {
        return pinned;
    }

    public boolean isSynced() {
        return synced;
    }

    public String getType() {
        return type;
    }

    public int getVersion() {
        return version;
    }

    public String getVisibleName() {
        return visibleName;
    }
}
