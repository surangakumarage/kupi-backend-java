package com.kupi.fileuploader.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileManagerDto {

    private final String fileName;
    private String description;
    private final String url;
    private final String userId;
    private final String contentType;
    private final long fileSize;
    private final String type;

    public FileManagerDto(String contentType, long contentLength, String url, String fileName, String userId) {
        this.fileName = fileName;
        this.userId = userId;
        this.contentType = contentType;
        this.fileSize = contentLength;
        this.url = url;
        this.type = contentType != null ? contentType.split("/")[0] : "unknown";
    }

}