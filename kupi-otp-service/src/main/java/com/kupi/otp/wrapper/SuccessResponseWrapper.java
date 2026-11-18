package com.kupi.otp.wrapper;

import com.kupi.otp.enums.ResponseStatusType;
import com.kupi.otp.enums.SuccessResponseStatusType;
import lombok.Getter;

/**
 * Success Response Wrapper
 */
@Getter
public class SuccessResponseWrapper extends ResponseWrapper {

    private final String displayMessage;

    public SuccessResponseWrapper(SuccessResponseStatusType successResponseStatusType, String message, String data) {
        super(ResponseStatusType.SUCCESS, successResponseStatusType.getMessage(), data);
        this.displayMessage = message;
    }
}
