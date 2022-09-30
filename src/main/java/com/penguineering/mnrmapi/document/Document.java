package com.penguineering.mnrmapi.document;

import com.penguineering.mnrmapi.index.DocumentIndex;

public class Document {
    public static Builder buildWithIndex(DocumentIndex di) {
        return new Builder(di);
    }

    public static class Builder {
        private final DocumentIndex index;
        private Content content;
        private Metadata metadata;

        public Builder(DocumentIndex index) {
            this.index = index;
        }

        public Builder setContent(Content content) {
            this.content = content;
            return this;
        }

        public Builder setMetadata(Metadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public Document build() {
            return new Document(index, content, metadata);
        }
    }

    private final DocumentIndex index;
    private final Content content;
    private final Metadata metadata;

    public Document(DocumentIndex index, Content content, Metadata metadata) {
        this.index = index;
        this.content = content;
        this.metadata = metadata;
    }

    public DocumentIndex getIndex() {
        return index;
    }

    public Content getContent() {
        return content;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder("Document{");

        builder.append("id=");
        builder.append(index.getDocId());
        builder.append(", parent='");
        builder.append(metadata.getParent());
        builder.append("'");
        if (metadata.getVisibleName() != null) {
            builder.append(", vName='");
            builder.append(metadata.getVisibleName());
            builder.append("'");
        }
        if (metadata.isDeleted())
            builder.append(", deleted");
        builder.append("}");

        return builder.toString();
    }
}
