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

    private final int statusCode;

    public SuccessResponseWrapper(ResponseStatusType status, SuccessResponseStatusType successResponseStatusType, ResponseDto responseDto, String displayMessage) {
        super(status, successResponseStatusType.getMessage(), responseDto, displayMessage);
        this.statusCode = successResponseStatusType.getCode();
    }
}
