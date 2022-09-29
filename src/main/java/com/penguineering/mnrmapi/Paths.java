package com.penguineering.mnrmapi;

import io.micronaut.http.uri.UriBuilder;

import java.net.URI;

public interface Paths {
    String SERVICE_MANAGER = "https://webapp-production-dot-remarkable-production.appspot.com";
    String USER_AUTH_PATH = "/token/json/2/user/new";
    URI USER_TOKEN_URI = UriBuilder.of(Paths.SERVICE_MANAGER).path(USER_AUTH_PATH).build();
    String API_URL = "https://rm-blob-storage-prod.appspot.com/api/v1";
    String API_DOWNLOAD_PATH = "/signed-urls/downloads";
    String API_DOWNLOAD = API_URL + API_DOWNLOAD_PATH;
    String API_UPLOAD = API_URL +  "/signed-urls/uploads";
    String SYNC_COMPLETE = API_URL + "/sync-complete";
}
