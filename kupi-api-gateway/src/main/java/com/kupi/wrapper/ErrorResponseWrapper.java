package com.kupi.wrapper;

import com.kupi.domain.response.ResponseDto;
import com.kupi.enums.ErrorResponseStatusType;
import com.kupi.enums.ResponseStatusType;
import lombok.Getter;

/**
 * ResponseWrapper
 */
@Getter
public class ErrorResponseWrapper extends ResponseWrapper {

    private final int errorCode;
    private final String displayMessage;

    public ErrorResponseWrapper(ErrorResponseStatusType errorResponseStatusType, String message,
                                ResponseDto data) {
        super(ResponseStatusType.ERROR, errorResponseStatusType.getMessage(), data);
        this.errorCode = errorResponseStatusType.getCode();
        this.displayMessage = message;
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}
