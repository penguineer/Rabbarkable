package com.penguineering.mnrmapi.gcs;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.time.Instant;

public class FileRequestResponse extends FileRequestBody {
    private final URI url;
    private final Instant expires;

    FileRequestResponse(@JsonProperty("http_method") String method,
                        @JsonProperty("relative_path") String gcsPath,
                        @JsonProperty("url") URI url,
                        @JsonProperty("expires") Instant expires) {
        super(method, gcsPath);
        this.url = url;
        this.expires = expires;
    }

    public URI getUrl() {
        return url;
    }

    public Instant getExpires() {
        return expires;
    }

    @Override
    public String toString() {
        return "Download path (expiry %s): %s".formatted(this.expires, this.url);
    }

}
