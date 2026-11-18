package com.kupi.otp.wrapper;

import com.kupi.otp.domain.BaseDto;
import com.kupi.otp.enums.ResponseStatusType;
import lombok.Getter;

/**
 * ResponseWrapper
 */
@Getter
public class ResponseWrapper implements BaseDto {

    private ResponseStatusType status;
    private String message;
    private String data;

    public ResponseWrapper(ResponseStatusType statusType, String message, String data) {
        this.status = statusType;
        this.message = message;
        this.data = data;
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}

