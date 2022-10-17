package com.penguineering.mnrmapi.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.penguineering.mnrmapi.GCS;
import com.penguineering.mnrmapi.index.DocumentIndex;
import io.micronaut.context.annotation.Bean;
import jakarta.inject.Inject;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Bean
public class DocumentAccess {
    @Inject
    GCS gcs;

    public Flux<Document> retrieveDocument(Flux<DocumentIndex> index) {
        return index
                .flatMap(i -> Flux.zip(
                        Mono.just(Document.buildWithIndex(i)),
                        Flux.just(i.getContentGCS()).transform(this::retrieveContent),
                        Flux.just(i.getMetadataGCS()).transform(this::retrieveMetaData)
                        ))
                .map(t -> t.getT1()
                        .setContent(t.getT2())
                        .setMetadata(t.getT3())
                        .build());
    }

    /** Calculate the number of documents in each folder
     *
     * @param documents A Flux of Document
     * @return Flux of Tuple2 with Folder ID and Document count
     */
    public Flux<Tuple2<String, Integer>> folderStatistics(Flux<Document> documents) {
        return documents
                .mapNotNull(Document::getMetadata)
                .filter(m -> !m.isDeleted())
                .reduce(new ConcurrentHashMap<String, AtomicInteger>(),
                        (map, metadata) -> {
                            map
                                    .computeIfAbsent(metadata.getParent(), i -> new AtomicInteger(0))
                                    .incrementAndGet();
                            return map;
                        })
                .map(ConcurrentHashMap::entrySet)
                .flatMapMany(Flux::fromIterable)
                .map(e -> Tuples.of(e.getKey(), e.getValue().get()));
    }

    public <T> Flux<T> unmarshall(Flux<String> result, Class<T> cls) {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        return result
                .handle((res, sink) -> {
                    try {
                        sink.next(mapper.readValue(res, cls));
                    } catch (JsonProcessingException e) {
                        sink.error(e);
                    }
                });
    }

    public Flux<Content> retrieveContent(Flux<String> gcsPath) {
        return gcsPath
                .transform(gcs::retrieve)
                .transform(result -> this.unmarshall(result, Content.class));
    }

    public Flux<Metadata> retrieveMetaData(Flux<String> gcsPath) {
        return gcsPath
                .transform(gcs::retrieve)
                .transform(result -> this.unmarshall(result, Metadata.class));
    }
}
