package com.kupi.fileuploader.domain.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.net.URL;

@Getter
@Setter
@NoArgsConstructor
public class CsvS3UploadResponseDto implements ResponseDto {

    private String fileUrl;
    private String fileName;

    public CsvS3UploadResponseDto(URL csvUrl, String fileName) {
        this.fileUrl = csvUrl == null ? null : csvUrl.toString();
        this.fileName = fileName;
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}
