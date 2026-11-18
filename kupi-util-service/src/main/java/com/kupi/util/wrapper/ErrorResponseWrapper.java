package com.kupi.util.wrapper;

import com.kupi.util.domain.response.ResponseDto;
import com.kupi.util.enums.ResponseStatusType;
import lombok.Getter;

/**
 * ErrorResponseWrapper
 */
@Getter
public class ErrorResponseWrapper extends ResponseWrapper {

    private final int errorCode;

    public ErrorResponseWrapper(ResponseStatusType status,
                                String message,
                                ResponseDto data, String displayMessage, int errorCode) {
        super(status, message, data, displayMessage);
        this.errorCode = errorCode;
    }

}
    