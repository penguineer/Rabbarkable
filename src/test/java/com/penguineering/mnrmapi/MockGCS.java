package com.penguineering.mnrmapi;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Bean
@Singleton  // this is a singleton for test verification
@Replaces(bean = GCS.class)
@Requires(env = {"test"}, property = "mockGCS")
public class MockGCS extends GCS {
    public Queue<String> gcs_responses = new LinkedList<>();

    public List<String> gcs_requests = new ArrayList<>();

    public void reset() {
        this.gcs_requests.clear();
        this.gcs_responses.clear();
    }

    @Override
    public Flux<String> retrieve(Flux<String> gcsPath) {
        return gcsPath
                .switchMap(path -> {
                    gcs_requests.add(path);
                    return Flux.just(gcs_responses.remove());
                });
    }
}
