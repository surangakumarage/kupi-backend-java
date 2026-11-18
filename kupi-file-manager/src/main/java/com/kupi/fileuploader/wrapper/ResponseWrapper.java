package com.kupi.fileuploader.wrapper;

import com.kupi.fileuploader.domain.response.ResponseDto;
import com.kupi.fileuploader.domain.BaseDto;
import com.kupi.fileuploader.enums.ResponseStatusType;
import lombok.Getter;

/**
 * ResponseWrapper
 */
@Getter
public class ResponseWrapper implements BaseDto {

    private ResponseStatusType status;
    private String message;
    private ResponseDto data;

    public ResponseWrapper(ResponseStatusType statusType, String message, ResponseDto data) {
        this.status = statusType;
        this.message = message;
        this.data = data;
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}
