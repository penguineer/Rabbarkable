package com.penguineering.mnrmapi.index;

import com.penguineering.mnrmapi.GCS;
import com.penguineering.mnrmapi.MockGCS;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@MicronautTest
@Property(name = "mockGCS")
public class TestIndexAccess {
    @Inject
    protected GCS gcs;

    @Inject
    protected IndexAccess index;

    @Test
    public void testRetrieveIndexMulti() {
        final MockGCS gcs = (MockGCS) this.gcs;
        gcs.reset();

        final String[] GCS_TEST_PATHS = {
                "062a56369e2c89c2228e7fef98bc37d75d7a9b781088d1a45d6a318dbf938ae0",
                "162a56369e2c89c2228e7fef98bc37d75d7a9b781088d1a45d6a318dbf938ae1"};
        final String[] GCS_INDEX_RESPONSES = {
                """
                3
                062a56369e2c89c2228e7fef98bc37d75d7a9b781088d1a45d6a318dbf938ae3:80000000:005de518-e1eb-45e9-8ceb-82f320a25c42:5:544144
                1213bba43381a44024e902e6830a2c89d9153dfab40dc914618dc69c2521a7e4:80000000:005de518-e1eb-45e9-8ceb-82f320a25c42:5:43930
                25287343e894836fffe2f9e210f60c9c0e479c3411363d5bfbd78f7466d1885c:80000000:005de518-e1eb-45e9-8ceb-82f320a25c42:2:277
                """,
                """
                3
                062a56369e2c89c2228e7fef98bc37d75d7a9b781088d1a45d6a318dbf938ae3:80000000:105de518-e1eb-45e9-8ceb-82f320a25c42:5:544144
                1213bba43381a44024e902e6830a2c89d9153dfab40dc914618dc69c2521a7e4:80000000:105de518-e1eb-45e9-8ceb-82f320a25c42:5:43930
                25287343e894836fffe2f9e210f60c9c0e479c3411363d5bfbd78f7466d1885c:80000000:105de518-e1eb-45e9-8ceb-82f320a25c42:2:277
                """
        };
        gcs.gcs_responses.addAll(Arrays.asList(GCS_INDEX_RESPONSES));

        StepVerifier.create(
                        index.retrieveIndex(Flux.fromIterable(Arrays.asList(GCS_TEST_PATHS))))
                .expectNext(new IndexEntry("062a56369e2c89c2228e7fef98bc37d75d7a9b781088d1a45d6a318dbf938ae3", "005de518-e1eb-45e9-8ceb-82f320a25c42", 5, 544144L))
                .expectNext(new IndexEntry("1213bba43381a44024e902e6830a2c89d9153dfab40dc914618dc69c2521a7e4", "005de518-e1eb-45e9-8ceb-82f320a25c42", 5, 43930L))
                .expectNext(new IndexEntry("25287343e894836fffe2f9e210f60c9c0e479c3411363d5bfbd78f7466d1885c", "005de518-e1eb-45e9-8ceb-82f320a25c42", 2, 277L))
                .expectNext(new IndexEntry("062a56369e2c89c2228e7fef98bc37d75d7a9b781088d1a45d6a318dbf938ae3", "105de518-e1eb-45e9-8ceb-82f320a25c42", 5, 544144L))
                .expectNext(new IndexEntry("1213bba43381a44024e902e6830a2c89d9153dfab40dc914618dc69c2521a7e4", "105de518-e1eb-45e9-8ceb-82f320a25c42", 5, 43930L))
                .expectNext(new IndexEntry("25287343e894836fffe2f9e210f60c9c0e479c3411363d5bfbd78f7466d1885c", "105de518-e1eb-45e9-8ceb-82f320a25c42", 2, 277L))
                .verifyComplete();

        // verify requests
        assertEquals(2, gcs.gcs_requests.size());
        assertEquals(GCS_TEST_PATHS[0], gcs.gcs_requests.get(0));
        assertEquals(GCS_TEST_PATHS[1], gcs.gcs_requests.get(1));
    }

    @Test
    public void testRetrieveRootGCS() {
        final MockGCS gcs = (MockGCS) this.gcs;
        gcs.reset();

        final String MOCK_ROOT_GCS = "mock-root-gcs-id";
        gcs.gcs_responses.add(MOCK_ROOT_GCS);

        StepVerifier.create(
                        index.retrieveRootGcs())
                .expectNext(MOCK_ROOT_GCS)
                .verifyComplete();

        assertEquals(1, gcs.gcs_requests.size());
        assertEquals("root", gcs.gcs_requests.get(0));
    }

    @Test
    public void testRetrieveRootIndex() {
        final MockGCS gcs = (MockGCS) this.gcs;
        gcs.reset();

        final String MOCK_ROOT_GCS = "mock-root-gcs-id";
        gcs.gcs_responses.add(MOCK_ROOT_GCS);

        final String GCS_INDEX_RESPONSE = """
                3
                062a56369e2c89c2228e7fef98bc37d75d7a9b781088d1a45d6a318dbf938ae3:80000000:005de518-e1eb-45e9-8ceb-82f320a25c42:5:544144
                1213bba43381a44024e902e6830a2c89d9153dfab40dc914618dc69c2521a7e4:80000000:005de518-e1eb-45e9-8ceb-82f320a25c42:5:43930
                25287343e894836fffe2f9e210f60c9c0e479c3411363d5bfbd78f7466d1885c:80000000:005de518-e1eb-45e9-8ceb-82f320a25c42:2:277
                """;
        gcs.gcs_responses.add(GCS_INDEX_RESPONSE);

        StepVerifier.create(
                        index.retrieveRootIndex())
                .expectNext(new IndexEntry("062a56369e2c89c2228e7fef98bc37d75d7a9b781088d1a45d6a318dbf938ae3", "005de518-e1eb-45e9-8ceb-82f320a25c42", 5, 544144L))
                .expectNext(new IndexEntry("1213bba43381a44024e902e6830a2c89d9153dfab40dc914618dc69c2521a7e4", "005de518-e1eb-45e9-8ceb-82f320a25c42", 5, 43930L))
                .expectNext(new IndexEntry("25287343e894836fffe2f9e210f60c9c0e479c3411363d5bfbd78f7466d1885c", "005de518-e1eb-45e9-8ceb-82f320a25c42", 2, 277L))
                .verifyComplete();

        // verify requests
        assertEquals(2, gcs.gcs_requests.size());
        assertEquals("root", gcs.gcs_requests.get(0));
        assertEquals(MOCK_ROOT_GCS, gcs.gcs_requests.get(1));

    }

    @Test
    public void testRetrieveDocumentIndexSuccess() {
        final MockGCS gcs = (MockGCS) this.gcs;
        gcs.reset();

        final String TEST_GCS = "162a56369e2c89c2228e7fef98bc37d75d7a9b781088d1a45d6a318dbf938ae3";
        final String TEST_DOC_ID = "005de518-e1eb-45e9-8ceb-82f320a25c42:5:544144";
        final IndexEntry TEST_INDEX_ENTRY = IndexEntry.fromLine(TEST_GCS + ":80000000:" + TEST_DOC_ID);
        final String TEST_INDEX = """
                3
                062a56369e2c89c2228e7fef98bc37d75d7a9b781088d1a45d6a318dbf938ae3:80000000:005de518-e1eb-45e9-8ceb-82f320a25c42:5:544144
                1213bba43381a44024e902e6830a2c89d9153dfab40dc914618dc69c2521a7e4:80000000:005de518-e1eb-45e9-8ceb-82f320a25c42:5:43930
                25287343e894836fffe2f9e210f60c9c0e479c3411363d5bfbd78f7466d1885c:80000000:005de518-e1eb-45e9-8ceb-82f320a25c42:2:277
                """;
        gcs.gcs_responses.add(TEST_INDEX);

        final List<IndexEntry> TEST_ENTRIES = IndexEntry.fromData(TEST_INDEX);
        assert TEST_ENTRIES.size() > 0;
        final DocumentIndex TEST_DOC_INDEX;
        {
            DocumentIndex.Builder b = DocumentIndex.buildFromIndexEntry(TEST_INDEX_ENTRY);
            TEST_ENTRIES.forEach(b::parseIndexEntry);
            TEST_DOC_INDEX = b.build();
        }

        StepVerifier.create(
                        index.retrieveDocumentIndex(Flux.just(TEST_INDEX_ENTRY)))
                .expectNext(TEST_DOC_INDEX)
                .verifyComplete();

        // verify requests
        assertEquals(1, gcs.gcs_requests.size());
        assertEquals(TEST_GCS, gcs.gcs_requests.get(0));
    }

    @Test
    public void testRetrieveDocumentIndexIdMismatch() {
        final MockGCS gcs = (MockGCS) this.gcs;
        gcs.reset();

        final String TEST_GCS = "162a56369e2c89c2228e7fef98bc37d75d7a9b781088d1a45d6a318dbf938ae3";
        final String TEST_DOC_ID = "005de518-e1eb-45e9-8ceb-82f320a25c42:5:544144";
        final IndexEntry TEST_INDEX_ENTRY = IndexEntry.fromLine(TEST_GCS + ":80000000:" + TEST_DOC_ID);
        // IDs do not match
        final String TEST_INDEX = """
                3
                062a56369e2c89c2228e7fef98bc37d75d7a9b781088d1a45d6a318dbf938ae3:80000000:005de518-e1eb-45e9-8ceb-82f320a25c42:5:544144
                1213bba43381a44024e902e6830a2c89d9153dfab40dc914618dc69c2521a7e4:80000000:105de518-e1eb-45e9-8ceb-82f320a25c42:5:43930
                25287343e894836fffe2f9e210f60c9c0e479c3411363d5bfbd78f7466d1885c:80000000:205de518-e1eb-45e9-8ceb-82f320a25c42:2:277
                """;
        gcs.gcs_responses.add(TEST_INDEX);

        final List<IndexEntry> TEST_ENTRIES = IndexEntry.fromData(TEST_INDEX);
        assert TEST_ENTRIES.size() > 0;

        StepVerifier.create(
                        index.retrieveDocumentIndex(Flux.just(TEST_INDEX_ENTRY)))
                .verifyError(IllegalArgumentException.class);

        // verify requests
        assertEquals(1, gcs.gcs_requests.size());
        assertEquals(TEST_GCS, gcs.gcs_requests.get(0));
    }
}
