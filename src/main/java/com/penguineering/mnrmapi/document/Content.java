package com.penguineering.mnrmapi.document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Content {

    private final int coverPageNumber;
    private final ContentMetadata contentMetadata;
    private final boolean dummyDocument;
    private final Map<String, String> extraMetadata;
    private final String fileType;  // notebook, pdf, epub
    private final String fontName;
    private final int lineHeight;
    private final int margins;
    private final String orientation;
    private final int originalPageCount;
    private final List<String> pages;
    private final List<String> redirectionPageMap;
    private final String sizeInBytes;
    private final String textAlignment;
    private final int textScale;
    private final Map<String, Integer> transform;


    public Content(@JsonProperty(value="coverPageNumber") int coverPageNumber,
                   @JsonProperty(value="documentMetadata") ContentMetadata contentMetadata,
                   @JsonProperty(value="dummyDocument") boolean dummyDocument,
                   @JsonProperty(value="extraMetadata") Map<String, String> extraMetadata,
                   @JsonProperty(value="fileType") String fileType,
                   @JsonProperty(value="fontName") String fontName,
                   @JsonProperty(value="lineHeight") int lineHeight,
                   @JsonProperty(value="margins") int margins,
                   @JsonProperty(value="orientation") String orientation,
                   @JsonProperty(value="originalPageCount") int originalPageCount,
                   @JsonProperty(value="pages") List<String> pages,
                   @JsonProperty(value="redirectionPageMap") List<String> redirectionPageMap,
                   @JsonProperty(value="sizeInBytes") String sizeInBytes,
                   @JsonProperty(value="textAlignment") String textAlignment,
                   @JsonProperty(value="textScale") int textScale,
                   @JsonProperty(value="transform") Map<String, Integer> transform) {
        this.coverPageNumber = coverPageNumber;
        this.contentMetadata = contentMetadata;
        this.dummyDocument = dummyDocument;
        this.extraMetadata = extraMetadata;
        this.fileType = fileType;
        this.fontName = fontName;
        this.lineHeight = lineHeight;
        this.margins = margins;
        this.orientation = orientation;
        this.originalPageCount = originalPageCount;
        this.pages = pages;
        this.redirectionPageMap = redirectionPageMap;
        this.sizeInBytes = sizeInBytes;
        this.textAlignment = textAlignment;
        this.textScale = textScale;
        this.transform = transform;
    }

    public int getCoverPageNumber() {
        return coverPageNumber;
    }

    public ContentMetadata getDocumentMetadata() {
        return contentMetadata;
    }

    public boolean isDummyDocument() {
        return dummyDocument;
    }

    public Map<String, String> getExtraMetadata() {
        return extraMetadata;
    }

    public String getFileType() {
        return fileType;
    }

    public String getFontName() {
        return fontName;
    }

    public int getLineHeight() {
        return lineHeight;
    }

    public int getMargins() {
        return margins;
    }

    public String getOrientation() {
        return orientation;
    }

    public int getOriginalPageCount() {
        return originalPageCount;
    }

    public List<String> getPages() {
        return pages;
    }

    public List<String> getRedirectionPageMap() {
        return redirectionPageMap;
    }

    public String getSizeInBytes() {
        return sizeInBytes;
    }

    public String getTextAlignment() {
        return textAlignment;
    }

    public int getTextScale() {
        return textScale;
    }

    public Map<String, Integer> getTransform() {
        return transform;
    }

    @JsonProperty(value="pageCount")
    public int getPageCount() {
        return pages == null ? 0 : pages.size();
    }
}
