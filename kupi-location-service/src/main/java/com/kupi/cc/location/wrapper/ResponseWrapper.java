package com.kupi.cc.location.wrapper;

import com.kupi.cc.location.domain.BaseDto;
import com.kupi.cc.location.domain.response.ResponseDto;
import com.kupi.cc.location.enums.ResponseStatusType;
import lombok.Getter;

/**
 * ResponseWrapper
 */
@Getter
public class ResponseWrapper implements BaseDto {

    private final ResponseStatusType status;
    private final String message;
    private final String displayMessage;
    private final ResponseDto data;

    public ResponseWrapper(ResponseStatusType statusType, String message, String displayMessage, ResponseDto data) {
        this.status = statusType;
        this.message = message;
        this.displayMessage = displayMessage;
        this.data = data;
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}

