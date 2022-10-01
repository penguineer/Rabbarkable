package com.penguineering.mnrmapi.discovery;

import com.penguineering.mnrmapi.Paths;
import io.micronaut.context.annotation.Bean;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.uri.UriBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

@Bean
public class ServiceDiscovery {

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

    public Mono<URI> fetchNotificationHost() {
        return Mono
                .just(this.notificationURI)
                .transform(this::retrieveHost)
                .map(host -> UriBuilder.of("").host(host).scheme("wss").path(Paths.NOTIFY_PATH).build());
    }

    public Mono<URI> fetchDocumentAPI() {
        return Mono
                // For some reason the service discovery for this part does not work at the moment.
                /*.just(this.documentURI)
                .transform(this::retrieveHost)
                .map(host -> UriBuilder.of("").scheme("https").host(host).path(Paths.API_DOWNLOAD_PATH).build()) */
                .just(UriBuilder.of(Paths.API_DOWNLOAD).build());

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
