package com.kupi.cc.location.wrapper;

import com.kupi.cc.location.domain.response.ResponseDto;
import com.kupi.cc.location.enums.ErrorResponseStatusType;
import com.kupi.cc.location.enums.ResponseStatusType;
import lombok.Getter;

/**
 * ErrorResponseWrapper
 */
@Getter
public class ErrorResponseWrapper extends ResponseWrapper {

    private final int errorCode;

    public ErrorResponseWrapper(ErrorResponseStatusType errorResponseStatusType, String message, ResponseDto data) {
        super(ResponseStatusType.ERROR, errorResponseStatusType.getMessage(), message, data);
        this.errorCode = errorResponseStatusType.getCode();
    }
}
