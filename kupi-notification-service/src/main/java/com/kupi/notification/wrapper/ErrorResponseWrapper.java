package com.kupi.notification.wrapper;

import com.kupi.notification.domain.response.ResponseDto;
import com.kupi.notification.enums.ErrorResponseStatusType;
import com.kupi.notification.enums.ResponseStatusType;
import lombok.Getter;

/**
 * ErrorResponseWrapper
 */
@Getter
public class ErrorResponseWrapper extends ResponseWrapper {

    private final int errorCode;

    public ErrorResponseWrapper(ErrorResponseStatusType errorResponseStatusType, String message, ResponseDto data) {
        super(ResponseStatusType.ERROR, errorResponseStatusType.getMessage(), data, message);
        this.errorCode = errorResponseStatusType.getCode();
    }

}
