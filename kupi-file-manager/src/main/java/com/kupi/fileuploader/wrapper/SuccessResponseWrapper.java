package com.kupi.fileuploader.wrapper;

import com.kupi.fileuploader.domain.response.ResponseDto;
import com.kupi.fileuploader.enums.ResponseStatusType;
import com.kupi.fileuploader.enums.SuccessResponseStatusType;
import lombok.Getter;

/**
 * Success Response Wrapper
 */
@Getter
public class SuccessResponseWrapper extends ResponseWrapper {

    private final String displayMessage;

    public SuccessResponseWrapper(SuccessResponseStatusType successResponseStatusType, String message,
                                  ResponseDto data) {
        super(ResponseStatusType.SUCCESS, successResponseStatusType.getMessage(), data);
        this.displayMessage = message;
    }
}
