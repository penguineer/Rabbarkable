package com.penguineering.mnrmapi.index;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


@MicronautTest
public class TestDocumentIndex {
    private static final String TEST_GCS = "062a56369e2c89c2228e7fef98bc37d75d7a9b781088d1a45d6a318dbf938ae3";
    private static final String TEST_ENTRY_GCS = "162a56369e2c89c2228e7fef98bc37d75d7a9b781088d1a45d6a318dbf938ae3";

    private static final String TEST_ID = "005de518-e1eb-45e9-8ceb-82f320a25c42";
    private static final String MISMATCH_ID = "105de518-e1eb-45e9-8ceb-82f320a25c42";


    private static DocumentIndex.Builder newDocumentIndexBuilder() {
        return DocumentIndex
                .buildFromIndexEntry(IndexEntry.fromLine(TEST_GCS + ":80000000:" + TEST_ID + ":5:544144"));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void testBuildFromIndexEntry() {
        // successful build
        final DocumentIndex.Builder b = newDocumentIndexBuilder();
        assertNotNull(b);

        assertDoesNotThrow(() -> {
            final DocumentIndex di = b.build();
            assertNotNull(di);
            assertEquals(TEST_GCS, di.getIndexGCS());
            assertEquals(TEST_ID, di.getDocId());
            assertEquals(0, di.length());
        });

        // test null input
        assertThrows(NullPointerException.class,
                () -> DocumentIndex.buildFromIndexEntry(null));
    }

    @Test
    void testBuilderParseIndexEntry() {
        // successfully parsing an index entry
        final DocumentIndex.Builder b = newDocumentIndexBuilder();
        assertDoesNotThrow(() ->
                b.parseIndexEntry(
                        new IndexEntry(TEST_ENTRY_GCS, TEST_ID, 1, 1L))
        );
        final DocumentIndex di = b.build();
        assertEquals(TEST_ENTRY_GCS, di.getAdditionalGCS().get(""));
        assertNull(di.getContentGCS());
        assertNull(di.getMetadataGCS());
        assertEquals(1, di.length());
    }

    @Test
    void testBuilderParseContentEntry() {
        // adding a content index entry
        final DocumentIndex.Builder b = newDocumentIndexBuilder();
        assertDoesNotThrow(() ->
                b.parseIndexEntry(
                        new IndexEntry(TEST_ENTRY_GCS, TEST_ID + ".content", 1, 1L))
        );
        final DocumentIndex di = b.build();
        assertEquals(TEST_ENTRY_GCS, di.getContentGCS());
        assertEquals(0, di.getAdditionalGCS().size());
        assertNull(di.getMetadataGCS());
        assertEquals(1, di.length());
    }

    @Test
    void testBuilderParseMetadataEntry() {
        // adding a metadata index entry
        final DocumentIndex.Builder b = newDocumentIndexBuilder();
        assertDoesNotThrow(() ->
                b.parseIndexEntry(
                        new IndexEntry(TEST_ENTRY_GCS, TEST_ID + ".metadata", 1, 1L))
        );
        final DocumentIndex di = b.build();
        assertEquals(TEST_ENTRY_GCS, di.getMetadataGCS());
        assertEquals(0, di.getAdditionalGCS().size());
        assertNull(di.getContentGCS());
        assertEquals(1, di.length());
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void testBuilderParseNullEntry() {
        // adding null as entry
        final DocumentIndex.Builder b = newDocumentIndexBuilder();
        assertThrows(NullPointerException.class,
                () -> b.parseIndexEntry(null));
    }

    @Test
    void testBuilderSetters() {
        DocumentIndex.Builder b = newDocumentIndexBuilder();
        b = b.setContentGCS("1");
        b = b.setMetadataGCS("2");
        b = b.addAdditionalGCS("test", "3");
        final DocumentIndex di = b.build();

        assertEquals("1", di.getContentGCS());
        assertEquals("2", di.getMetadataGCS());
        assertEquals("3", di.getAdditionalGCS().get("test"));
        assertEquals(3, di.length());
    }

    @Test
    void testDocIdMismatch() {
        DocumentIndex.Builder b = newDocumentIndexBuilder();
        assertThrows(IllegalArgumentException.class,
                () -> b.parseIndexEntry(new IndexEntry(TEST_GCS, MISMATCH_ID, 1, 1L)));
    }

}
