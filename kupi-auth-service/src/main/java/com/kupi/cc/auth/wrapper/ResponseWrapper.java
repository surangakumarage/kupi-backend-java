package com.kupi.cc.auth.wrapper;

import com.kupi.cc.auth.domain.BaseDto;
import com.kupi.cc.auth.enums.ResponseStatusType;
import com.kupi.cc.auth.domain.response.ResponseDto;
import lombok.Getter;

/**
 * ResponseWrapper
 */
@Getter
public class ResponseWrapper implements BaseDto {

    private ResponseStatusType status;
    private String message;
    private ResponseDto data;
    private String displayMessage;

    public ResponseWrapper(ResponseStatusType statusType, String message, ResponseDto data) {
        this.status = statusType;
        this.message = message;
        this.data = data;
        this.displayMessage = null;
    }

    public ResponseWrapper(ResponseStatusType statusType, String message, ResponseDto data, String displayMessage) {
        this.status = statusType;
        this.message = message;
        this.data = data;
        this.displayMessage = displayMessage;
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}

