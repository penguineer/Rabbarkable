package com.penguineering.mnrmapi.index;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class IndexEntry {
    private static final Pattern LINE_ENDINGS = Pattern.compile("\n");

    public static IndexEntry fromLine(String line) {
        String[] parts = line.split(":");
        if (parts.length < 5)
            throw new IllegalArgumentException("Line cannot be split into 5 parts: " + line);

        final String gcsPath = parts[0];
        final String id = parts[2];
        final Integer entriesCount = Integer.parseInt(parts[3]);
        final Long size = Long.parseLong(parts[4]);

        return new IndexEntry(gcsPath, id, entriesCount, size);
    }

    public static List<IndexEntry> fromData(String data) {
        return LINE_ENDINGS
                .splitAsStream(data)
                .skip(1)
                .map(IndexEntry::fromLine)
                .toList();
    }

    private final String gcsPath;
    private final String id;
    private final Integer entriesCount;
    private final Long size;

    IndexEntry(String gcsPath, String id, Integer entriesCount, Long size) {
        this.gcsPath = gcsPath;
        this.id = id;
        this.entriesCount = entriesCount;
        this.size = size;
    }

    public String getGcsPath() {
        return gcsPath;
    }

    public String getId() {
        return id;
    }

    public Integer getEntriesCount() {
        return entriesCount;
    }

    public Long getSize() {
        return size;
    }

    @Override
    public String toString() {
        return "IndexEntry{" +
                "gcsPath='" + gcsPath + '\'' +
                ", id='" + id + '\'' +
                ", entriesCount=" + entriesCount +
                ", size=" + size +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IndexEntry that)) return false;
        return Objects.equals(gcsPath, that.gcsPath) && Objects.equals(id, that.id) && Objects.equals(entriesCount, that.entriesCount) && Objects.equals(size, that.size);
    }
}
