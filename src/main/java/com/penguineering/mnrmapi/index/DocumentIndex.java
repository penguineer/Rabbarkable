package com.penguineering.mnrmapi.index;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An index representing a document on the re:markable tablet.
 */
public class DocumentIndex {
    public static Builder buildFromIndexEntry(IndexEntry docRoot) {
        return new Builder(docRoot.getGcsPath(), docRoot.getId());
    }

    public static class Builder {
        private final String indexGCS;

        private final String docId;

        private String contentGCS = null;
        private String metadataGCS = null;

        private final Map<String, String> additionalGCS = new HashMap<>();

        Builder(String indexGCS, String docId) {
            this.indexGCS = indexGCS;
            this.docId = docId;
        }

        public Builder parseIndexEntry(IndexEntry entry) {
            final String id = entry.getId();

            // split doc id
            if (!id.startsWith(this.docId))
                throw new IllegalArgumentException("Document ID mismatch!");

            final String suffix = id.substring(this.docId.length());
            if (suffix.equals(".content"))
                return this.setContentGCS(entry.getGcsPath());

            if (suffix.equals(".metadata"))
                return this.setMetadataGCS(entry.getGcsPath());

            return this.addAdditionalGCS(suffix, entry.getGcsPath());
        }

        public Builder setContentGCS(String contentGCS) {
            this.contentGCS = contentGCS;
            return this;
        }

        public Builder setMetadataGCS(String metadataGCS) {
            this.metadataGCS = metadataGCS;
            return this;
        }

        public Builder addAdditionalGCS(String key, String gcs) {
            this.additionalGCS.put(key, gcs);
            return this;
        }

        public DocumentIndex build() {
            return new DocumentIndex(
                    indexGCS,
                    docId,
                    contentGCS,
                    metadataGCS,
                    Collections.unmodifiableMap(additionalGCS));
        }
    }

    private final String indexGCS;
    private final String docId;
    private final String contentGCS;
    private final String metadataGCS;
    private final Map<String, String> additionalGCS;

    DocumentIndex(String indexGCS,
                  String docId,
                  String contentGCS,
                  String metadataGCS,
                  Map<String, String> additionalGCS) {
        this.indexGCS = indexGCS;
        this.docId = docId;
        this.contentGCS = contentGCS;
        this.metadataGCS = metadataGCS;
        this.additionalGCS = additionalGCS;
    }

    public String getIndexGCS() {
        return indexGCS;
    }

    public String getDocId() {
        return docId;
    }

    public String getContentGCS() {
        return contentGCS;
    }

    public String getMetadataGCS() {
        return metadataGCS;
    }

    public Map<String, String> getAdditionalGCS() {
        return additionalGCS;
    }

    public int length() {
        return (contentGCS == null ? 0 : 1)
                + (metadataGCS == null ? 0 : 1)
                + (additionalGCS == null ? 0 : additionalGCS.size());
    }

    @Override
    public String toString() {
        return "DocumentIndex{" +
                "docId='" + docId + '\'' +
                ", indexGCS='" + indexGCS + '\'' +
                ", length=" + length() +
                '}';
    }
}
