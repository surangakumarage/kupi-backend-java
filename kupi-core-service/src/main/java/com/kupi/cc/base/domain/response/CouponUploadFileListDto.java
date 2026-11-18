package com.kupi.cc.base.domain.response;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Getter
@Setter
public class CouponUploadFileListDto extends ResponseDto {

    private List<String> fileNames;

    public CouponUploadFileListDto(List<String> successFileNames, List<String> failedFileNames) {
        successFileNames.addAll(failedFileNames);
        HashSet<String> fileNamesSet = new HashSet<>(successFileNames);
        this.fileNames = new ArrayList<>(fileNamesSet);
    }
}
