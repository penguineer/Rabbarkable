package com.penguineering.mnrmapi.index;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@MicronautTest
public class TestIndexEntry {
    @Test
    @SuppressWarnings("ConstantConditions") // as we test for these
    public void testIndexEntryFromLine() {
        // successful creation
        assertDoesNotThrow(() -> {
            IndexEntry ie = IndexEntry.fromLine("062a56369e2c89c2228e7fef98bc37d75d7a9b781088d1a45d6a318dbf938ae3:80000000:005de518-e1eb-45e9-8ceb-82f320a25c42:5:544144");
            assertEquals("062a56369e2c89c2228e7fef98bc37d75d7a9b781088d1a45d6a318dbf938ae3", ie.getGcsPath());
            assertEquals("005de518-e1eb-45e9-8ceb-82f320a25c42", ie.getId());
            assertEquals(5, ie.getEntriesCount());
            assertEquals(544144, ie.getSize());
        });

        // null argument
        assertThrows(NullPointerException.class,
                () -> IndexEntry.fromLine(null));

        // empty line
        assertThrows(IllegalArgumentException.class,
                () -> IndexEntry.fromLine(""));

        // line with too few parts
        assertThrows(IllegalArgumentException.class,
                () -> IndexEntry.fromLine("062a56369e2c89c2228e7fef98bc37d75d7a9b781088d1a45d6a318dbf938ae3:80000000:005de518-e1eb-45e9-8ceb-82f320a25c42:5"));

        // line with too many
        assertThrows(IllegalArgumentException.class,
                () -> IndexEntry.fromLine("062a56369e2c89c2228e7fef98bc37d75d7a9b781088d1a45d6a318dbf938ae3:80000000:005de518-e1eb-45e9-8ceb-82f320a25c42:5:6:7"));

        // invalid integer for entry count
        assertThrows(NumberFormatException.class,
                () -> IndexEntry.fromLine("062a56369e2c89c2228e7fef98bc37d75d7a9b781088d1a45d6a318dbf938ae3:80000000:005de518-e1eb-45e9-8ceb-82f320a25c42:invalid:544144"));

        // invalid long for size
        assertThrows(NumberFormatException.class,
                () -> IndexEntry.fromLine("062a56369e2c89c2228e7fef98bc37d75d7a9b781088d1a45d6a318dbf938ae3:80000000:005de518-e1eb-45e9-8ceb-82f320a25c42:5:invalid"));
    }

    @Test
    void testIndexEntryFromData() {
        // successful creation
        assertDoesNotThrow(() -> {
            List<IndexEntry> entries = IndexEntry.fromData("""
                    3
                    062a56369e2c89c2228e7fef98bc37d75d7a9b781088d1a45d6a318dbf938ae3:80000000:005de518-e1eb-45e9-8ceb-82f320a25c42:5:544144
                    162a56369e2c89c2228e7fef98bc37d75d7a9b781088d1a45d6a318dbf938ae3:80000000:105de518-e1eb-45e9-8ceb-82f320a25c42:6:544145
                    """);
            assertNotNull(entries);
            assertEquals(2, entries.size());
            assertEquals(new IndexEntry("062a56369e2c89c2228e7fef98bc37d75d7a9b781088d1a45d6a318dbf938ae3", "005de518-e1eb-45e9-8ceb-82f320a25c42", 5, 544144L), entries.get(0));
            assertEquals(new IndexEntry("162a56369e2c89c2228e7fef98bc37d75d7a9b781088d1a45d6a318dbf938ae3", "105de518-e1eb-45e9-8ceb-82f320a25c42", 6, 544145L), entries.get(1));
        });

        // test with null input
        assertThrows(NullPointerException.class,
                () -> IndexEntry.fromData(null));

        // test with empty input
        assertDoesNotThrow(() -> {
            List<IndexEntry> entries = IndexEntry.fromData("");
            assertNotNull(entries);
            assertEquals(0, entries.size());
        });

        // test with only header line
        assertDoesNotThrow(() -> {
            List<IndexEntry> entries = IndexEntry.fromData("""
                    3
                    """);
            assertNotNull(entries);
            assertEquals(0, entries.size());
        });

        // test with invalid index entry
        assertThrows(NumberFormatException.class,
                () -> IndexEntry.fromData("""
                        3
                        062a56369e2c89c2228e7fef98bc37d75d7a9b781088d1a45d6a318dbf938ae3:80000000:005de518-e1eb-45e9-8ceb-82f320a25c42:5:544144
                        062a56369e2c89c2228e7fef98bc37d75d7a9b781088d1a45d6a318dbf938ae3:80000000:005de518-e1eb-45e9-8ceb-82f320a25c42:invalid:544144
                        """));
    }
}
