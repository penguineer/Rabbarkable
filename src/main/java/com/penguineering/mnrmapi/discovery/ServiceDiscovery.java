package com.penguineering.mnrmapi.discovery;

import io.micronaut.context.annotation.Bean;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.uri.UriBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

@Bean
public class ServiceDiscovery {
    private static final String SERVICE_MANAGER = "https://webapp-production-dot-remarkable-production.appspot.com";
    private static final String USER_AUTH_PATH = "/token/json/2/user/new";
    private static final URI USER_TOKEN_URI = UriBuilder.of(SERVICE_MANAGER).path(USER_AUTH_PATH).build();
    private static final URI API_URI = URI.create("https://rm-blob-storage-prod.appspot.com/api/v1");
    private static final String API_DOWNLOAD_PATH = "/signed-urls/downloads";
    private static final URI API_DOWNLOAD = UriBuilder.of(API_URI).path(API_DOWNLOAD_PATH).build();
    private static final String API_UPLOAD = API_URI +  "/signed-urls/uploads";
    private static final String SYNC_COMPLETE = API_URI + "/sync-complete";
    private static final URI NOTIFICATION_DISCOVERY_URI = UriBuilder.of("https://service-manager-production-dot-remarkable-production.appspot.com/service/json/1/notifications?environment=production&apiVer=1").build();
    private static final String NOTIFY_PATH = "/notifications/ws/json/1";


    private final HttpClient httpClient;
    // private final URI documentURI;

    public ServiceDiscovery(@Client HttpClient httpClient) {
        this.httpClient = httpClient;

        // This URI currently does not work
        /* this.documentURI = UriBuilder.of("https://service-manager-production-dot-remarkable-production.appspot.com/service/json/1/document-storage?environment=production&group=auth0%7C5a68dc51cb30df1234567890&apiVer=2")
                .build(); */
    }

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
                .map(ServiceDiscovery::notificationUriFromHost);
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
     * @return Endpoint URI
     */
    private Mono<String> retrieveHost(Mono<URI> uri) {
        return uri
                .map(HttpRequest::GET)
                .map(req -> httpClient.retrieve(req,  ServiceDiscoveryResult.class))
                .flatMap(Mono::from)
                .switchIfEmpty(Mono.error(new Exception("Service discovery did not answer!")))
                // TODO handle non-OK values
                .map(ServiceDiscoveryResult::getHost);
    }
}
