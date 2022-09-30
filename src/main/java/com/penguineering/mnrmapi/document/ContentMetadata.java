package com.penguineering.mnrmapi.document;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ContentMetadata {
    private final List<String> authors;
    private final String title;

    public ContentMetadata(@JsonProperty(value = "authors") List<String> authors,
                           @JsonProperty(value = "title") String title) {
        this.authors = authors;
        this.title = title;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return "DocumentMetadata{" +
                "authors=" + authors +
                ", title='" + title + '\'' +
                '}';
    }
}
