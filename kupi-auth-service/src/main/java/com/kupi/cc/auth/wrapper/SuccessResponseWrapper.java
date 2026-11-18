package com.kupi.cc.auth.wrapper;

import com.kupi.cc.auth.enums.ResponseStatusType;
import com.kupi.cc.auth.enums.SuccessResponseStatusType;
import com.kupi.cc.auth.domain.response.ResponseDto;
import lombok.Getter;

/**
 * Success Response Wrapper
 */
@Getter
public class SuccessResponseWrapper extends ResponseWrapper {

    public SuccessResponseWrapper(SuccessResponseStatusType successResponseStatusType, String message,
                                  ResponseDto data) {
        super(ResponseStatusType.SUCCESS, successResponseStatusType.getMessage(), data, message);
    }

    public SuccessResponseWrapper(SuccessResponseStatusType successResponseStatusType, String message,
                                  ResponseDto data, String customDisplayMessage) {
        super(ResponseStatusType.SUCCESS, successResponseStatusType.getMessage(), data, customDisplayMessage);
    }
}
