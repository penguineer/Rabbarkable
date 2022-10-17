package com.penguineering.mnrmapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.context.annotation.Bean;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import jakarta.inject.Inject;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.time.Instant;

import static io.micronaut.http.HttpHeaders.ACCEPT;

@Bean
public class GCS {
    @Inject
    protected HttpClient httpClient;

    @Inject
    protected Authentication auth;

    @Inject
    protected Discovery discovery;

    /**
     * Transform a GCS path to a storage URL
     * @param gcsPath Mono with GCS path as String
     * @return Mono with storage URI
     */
    public Flux<URI> lookup(Flux<String> gcsPath) {
        return gcsPath
                .map(FileRequestBody::withGet)
                .zipWith(
                        // Repeat this result, but not the look-up
                        discovery.fetchDownloadURI().repeat(),
                        (body, uri) -> HttpRequest
                                .POST(uri, body)
                                .header(ACCEPT, "application/json"))
                .transform(auth::userAuthenticatedRequest)
                .map(req -> httpClient.retrieve(req, FileRequestResponse.class))
                .flatMap(Flux::from)
                .map(FileRequestResponse::getUrl);
    }

    /**
     * Retrieve the content from a GCS path after lookup
     * @param gcsPath Mono with GCS path as String
     * @return downloaded content from this path
     */
    public Flux<String> retrieve(Flux<String> gcsPath) {
        return gcsPath
                .transform(this::lookup)
                .map(HttpRequest::GET)
                .map(httpClient::retrieve)
                .flatMap(Flux::from);
    }

    static class FileRequestBody {
        public static FileRequestBody withGet(String gcsPath) {
            return new FileRequestBody("GET", gcsPath);
        }

        @JsonProperty("http_method")
        final String method;

        @JsonProperty("relative_path")
        final String gcsPath;

        FileRequestBody(@JsonProperty("http_method") String method,
                        @JsonProperty("relative_path") String gcsPath) {
            this.method = method;
            this.gcsPath = gcsPath;
        }
    }

    static class FileRequestResponse extends FileRequestBody {
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
}
