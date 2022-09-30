package com.penguineering.mnrmapi.index;

import com.penguineering.mnrmapi.gcs.GcsAccess;
import io.micronaut.context.annotation.Bean;
import jakarta.inject.Inject;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Bean
public class IndexAccess {
    @Inject
    protected GcsAccess gcsAccess;

    public Flux<IndexEntry> retrieveIndex(Flux<String> gcsPath) {
        return gcsPath
                .transform(gcsAccess::retrieve)
                .map(IndexEntry::fromData)
                .flatMap(Flux::fromIterable);
    }

    public Flux<String> retrieveRootGcs() {
        return Flux
                .just("root")
                .transform(gcsAccess::retrieve);
    }

    public Flux<IndexEntry> retrieveRootIndex() {
        return retrieveRootGcs()
                .transform(this::retrieveIndex);
    }

    public Flux<DocumentIndex> retrieveDocumentIndex(Flux<IndexEntry> entries) {
        return entries
                .flatMap(e ->
                        Mono.just(DocumentIndex.buildFromIndexEntry(e))
                                .repeat()
                                .zipWith(
                                        Flux.just(e.getGcsPath())
                                                .transform(this::retrieveIndex))
                                .map(t -> t.getT1().parseIndexEntry(t.getT2()))
                                .last()
                                .map(DocumentIndex.Builder::build));
    }
}
