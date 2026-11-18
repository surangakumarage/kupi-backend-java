package com.kupi.fileuploader.domain.response;

import com.kupi.fileuploader.domain.entity.FileManager;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
public class FileSummaryPageResponse extends PageResponseDto {

    private FileSummaryResponse fileSummaryResponse;
    private List<FileManager> files;

    public FileSummaryPageResponse(Page<FileManager> page, FileSummaryResponse fileSummaryResponse,
                                   List<FileManager> files) {
        super(page);
        this.fileSummaryResponse = fileSummaryResponse;
        this.files = files;
    }

}
