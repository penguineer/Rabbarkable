package com.penguineering.mnrmapi.gcs;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FileRequestBody {
    public static FileRequestBody withGet(String gcsPath) {
        return new FileRequestBody("GET", gcsPath);
    }

    private final String method;
    private final String gcsPath;

    FileRequestBody(@JsonProperty("http_method") String method,
                    @JsonProperty("relative_path") String gcsPath) {
        this.method = method;
        this.gcsPath = gcsPath;
    }

    @JsonProperty("http_method")
    public String getMethod() {
        return method;
    }

    @JsonProperty("relative_path")
    public String getGcsPath() {
        return gcsPath;
    }
}
