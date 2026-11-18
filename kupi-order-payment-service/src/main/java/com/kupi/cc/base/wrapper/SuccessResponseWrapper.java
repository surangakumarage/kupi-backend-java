package com.kupi.cc.base.wrapper;

import com.kupi.cc.base.domain.response.ResponseDto;
import com.kupi.cc.base.enums.ResponseStatusType;
import com.kupi.cc.base.enums.SuccessResponseStatusType;
import lombok.Getter;

/**
 * Success Response Wrapper
 */
@Getter
public class SuccessResponseWrapper extends ResponseWrapper {

    private final int successCode;
    private final String displayMessage;

    public SuccessResponseWrapper(SuccessResponseStatusType successResponseStatusType, String message,
                                  ResponseDto data) {
        super(ResponseStatusType.SUCCESS, successResponseStatusType.getMessage(), data);
        this.successCode = successResponseStatusType.getCode();
        this.displayMessage = message;
    }
}
