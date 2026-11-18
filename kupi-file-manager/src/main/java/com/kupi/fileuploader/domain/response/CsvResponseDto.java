package com.kupi.fileuploader.domain.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class CsvResponseDto implements ResponseDto {

    private String csvUrl;
    private String contentType;
    private long csvByteSize;
    private String fileName;

    public CsvResponseDto(MultipartFile multipartFile, CsvS3UploadResponseDto csvS3UploadResponseDto) {
        this.contentType = multipartFile.getContentType();
        this.csvByteSize = multipartFile.getSize();
        this.csvUrl = null == csvS3UploadResponseDto ? null : csvS3UploadResponseDto.getFileUrl();
        this.fileName = null == csvS3UploadResponseDto ? null : csvS3UploadResponseDto.getFileName();
    }


    @Override
    public String toLogJson() {
        return toJson();
    }
}
