package com.penguineering.mnrmapi.gcs;

import com.penguineering.mnrmapi.Authentication;
import com.penguineering.mnrmapi.Discovery;
import io.micronaut.context.annotation.Bean;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import jakarta.inject.Inject;
import reactor.core.publisher.Flux;

import java.net.URI;

import static io.micronaut.http.HttpHeaders.ACCEPT;

@Bean
public class GcsAccess {
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
}
