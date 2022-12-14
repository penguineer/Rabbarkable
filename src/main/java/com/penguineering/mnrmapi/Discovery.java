package com.penguineering.mnrmapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.context.annotation.Bean;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.uri.UriBuilder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * Service endpoint discovery.
 *
 * <p>Construct or discover paths for service endpoints.</p>
 */
@Bean
@Singleton
public class Discovery {
    static final String SERVICE_MANAGER = "https://webapp-production-dot-remarkable-production.appspot.com";
    static final String USER_AUTH_PATH = "/token/json/2/user/new";
    static final URI USER_TOKEN_URI = UriBuilder.of(SERVICE_MANAGER).path(USER_AUTH_PATH).build();
    static final URI API_URI = URI.create("https://rm-blob-storage-prod.appspot.com/api/v1");
    static final String API_DOWNLOAD_PATH = "/signed-urls/downloads";
    static final URI API_DOWNLOAD = UriBuilder.of(API_URI).path(API_DOWNLOAD_PATH).build();

    /* These URIs will be used later
    private static final String API_UPLOAD = API_URI +  "/signed-urls/uploads";
    private static final String SYNC_COMPLETE = API_URI + "/sync-complete";
     */

    /* This URI does not work (currently/anymore?)
    private static final URI DOWNLOAD_DISCOVERY_URI = UriBuilder.of("https://service-manager-production-dot-remarkable-production.appspot.com/service/json/1/document-storage?environment=production&group=auth0%7C5a68dc51cb30df1234567890&apiVer=2").build();
     */

    static final URI NOTIFICATION_DISCOVERY_URI = UriBuilder.of("https://service-manager-production-dot-remarkable-production.appspot.com/service/json/1/notifications?environment=production&apiVer=1").build();
    private static final String NOTIFY_PATH = "/notifications/ws/json/1";

    @Inject
    protected HttpClient httpClient;

    /**
     * Fetch the URI for the token renewal endpoint.
     *
     * @return URI to call for token renewal
     */
    public Mono<URI> fetchUserTokenURI() {
        return Mono.just(USER_TOKEN_URI);
    }

    /**
     * Convert a notification host from discovery to the endpoint URI.
     *
     * @param host Host from discovery
     * @return A URI to the notification endpoint
     */
    private static URI notificationUriFromHost(String host) {
        return UriBuilder.of("").host(host).scheme("wss").path(NOTIFY_PATH).build();
    }

    /**
     * Fetch the URI for the notification endpoint.
     *
     * @return URI to call for notifications
     */
    public Mono<URI> fetchNotificationURI() {
        return Mono
                .just(NOTIFICATION_DISCOVERY_URI)
                .transform(this::retrieveHost)
                .map(Discovery::notificationUriFromHost);
    }

    /**
     * Fetch the URI for document download.
     * @return URI to call for downloads
     */
    public Mono<URI> fetchDownloadURI() {
        return Mono
                // For some reason the service discovery for this part does not work at the moment.
                /*.just(this.documentURI)
                .transform(this::retrieveHost)
                .map(host -> UriBuilder.of("").scheme("https").host(host).path(Paths.API_DOWNLOAD_PATH).build()) */
                .just(API_DOWNLOAD);
    }

    /**
     * Resolve a URI to the endpoint from the discovery service.
     *
     * @param uri URI identifier to resolve
     * @throws IllegalStateException is emitted if the discovery fails.
     * @return Endpoint URI
     */
    private Mono<String> retrieveHost(Mono<URI> uri) {
        // TODO we could do with a delayed retry if the first discovery fails
        return uri
                .map(HttpRequest::GET)
                .map(req -> httpClient.retrieve(req,  ServiceDiscoveryResult.class))
                .flatMap(Mono::from)
                .switchIfEmpty(Mono.error(new IllegalStateException("Service discovery did not answer!")))
                .flatMap(res -> res.status.equals("OK")
                        ? Mono.just(res.host)
                        : Mono.error(new IllegalStateException("Service discovery was not OK: "+ res.status)));
    }

    /**
     * Representation of the service discovery result
     * @param status The recovery status, must be "OK"
     * @param host Host used to construct the endpoint URI
     */
    private record ServiceDiscoveryResult(String status, String host) {
            private ServiceDiscoveryResult(@JsonProperty(value = "Status") String status,
                                           @JsonProperty(value = "Host") String host) {
                this.status = status;
                this.host = host;
            }
        }
}
