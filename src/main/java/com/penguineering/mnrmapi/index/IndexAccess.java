package com.penguineering.mnrmapi.index;

import com.penguineering.mnrmapi.GCS;
import io.micronaut.context.annotation.Bean;
import jakarta.inject.Inject;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Bean
public class IndexAccess {
    @Inject
    protected GCS gcs;

    /**
     * Retrieve and parse an index entry for a given stream of GCS paths.
     *
     * <p>Note that this function will accept multiple GCS paths as input, but it cannot
     * be distinguished which entries come from which GCS path, unless one call per path
     * is initiated.</p>
     *
     * @param gcsPath Flux with GCS path as String
     * @return Stream of index entries
     */
    public Flux<IndexEntry> retrieveIndex(Flux<String> gcsPath) {
        return gcsPath
                .transform(gcs::retrieve)
                .map(IndexEntry::fromData)
                .flatMap(Flux::fromIterable);
    }

    /**
     * Retrieve the GCS path of the index root.
     *
     * <p>This method returns a Flux for compatibility with follow-up calls</p>
     *
     * @return GSC path for the index root as flux.
     */
    public Flux<String> retrieveRootGcs() {
        return Flux
                .just("root")
                .transform(gcs::retrieve);
    }

    /**
     * Retrieve the root index.
     *
     * @return Flux of entries of the root index.
     */
    public Flux<IndexEntry> retrieveRootIndex() {
        return retrieveRootGcs()
                .transform(this::retrieveIndex);
    }

    /**
     * Retrieve an index that describes a document.
     *
     * <p>Documents consist of multiple files described in a document index.</p>
     *
     * <p>This method retrieves the document index based on its index entry and builds
     * a document index, which determines the GCS path for specific content elements,
     * such as meta data or actual document content.</p>
     *
     * @param entries Stream of index entries, each pointing to a document index
     * @return Stream of documents for each index entry
     */
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
