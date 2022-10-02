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
    String SERVICE_MANAGER = "https://webapp-production-dot-remarkable-production.appspot.com";
    String USER_AUTH_PATH = "/token/json/2/user/new";
    URI USER_TOKEN_URI = UriBuilder.of(SERVICE_MANAGER).path(USER_AUTH_PATH).build();
    String API_URL = "https://rm-blob-storage-prod.appspot.com/api/v1";
    String API_DOWNLOAD_PATH = "/signed-urls/downloads";
    String API_DOWNLOAD = API_URL + API_DOWNLOAD_PATH;
    String API_UPLOAD = API_URL +  "/signed-urls/uploads";
    String SYNC_COMPLETE = API_URL + "/sync-complete";
    String NOTIFY_PATH = "/notifications/ws/json/1";


    private final HttpClient httpClient;
    // private final URI documentURI;
    private final URI notificationURI;

    public ServiceDiscovery(@Client HttpClient httpClient) {
        this.httpClient = httpClient;

        // This URI currently does not work
        /* this.documentURI = UriBuilder.of("https://service-manager-production-dot-remarkable-production.appspot.com/service/json/1/document-storage?environment=production&group=auth0%7C5a68dc51cb30df1234567890&apiVer=2")
                .build(); */
        this.notificationURI = UriBuilder.of("https://service-manager-production-dot-remarkable-production.appspot.com/service/json/1/notifications?environment=production&apiVer=1")
                .build();
    }

    /**
     * Fetch the URI for the token renewal endpoint.
     *
     * @return URI to call for token renewal
     */
    public Mono<URI> fetchUserTokenURI() {
        return Mono.just(USER_TOKEN_URI);
    }

    public Mono<URI> fetchNotificationHost() {
        return Mono
                .just(this.notificationURI)
                .transform(this::retrieveHost)
                .map(host -> UriBuilder.of("").host(host).scheme("wss").path(NOTIFY_PATH).build());
    }

    public Mono<URI> fetchDocumentAPI() {
        return Mono
                // For some reason the service discovery for this part does not work at the moment.
                /*.just(this.documentURI)
                .transform(this::retrieveHost)
                .map(host -> UriBuilder.of("").scheme("https").host(host).path(Paths.API_DOWNLOAD_PATH).build()) */
                .just(UriBuilder.of(API_DOWNLOAD).build());

    }

    public Mono<String> retrieveHost(Mono<URI> uri) {
        return uri
                .map(HttpRequest::GET)
                .map(req -> httpClient.retrieve(req,  ServiceDiscoveryResult.class))
                .flatMap(Mono::from)
                .switchIfEmpty(Mono.error(new Exception("Service discovery did not answer!")))
                // TODO handle non-OK values
                .map(ServiceDiscoveryResult::getHost);
    }
}
