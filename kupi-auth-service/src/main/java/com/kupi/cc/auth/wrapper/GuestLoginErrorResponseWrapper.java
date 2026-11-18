package com.kupi.cc.auth.wrapper;

import com.kupi.cc.auth.domain.response.ResponseDto;
import com.kupi.cc.auth.enums.ErrorResponseStatusType;
import com.kupi.cc.auth.enums.ResponseStatusType;
import lombok.Getter;

/**
 * GuestLoginErrorResponseWrapper
 */
@Getter
public class GuestLoginErrorResponseWrapper implements ResponseDto {

    private final int errorCode;
    private final String displayMessage;
    private final ResponseStatusType status;
    private final String message;
    private final ResponseDto data;

    public GuestLoginErrorResponseWrapper(ErrorResponseStatusType errorResponseStatusType, String message) {

        this.status = ResponseStatusType.ERROR;
        this.message = errorResponseStatusType.getMessage();
        this.errorCode = errorResponseStatusType.getCode();
        this.displayMessage = message;
        this.data = null;
    }

    /**
     * This method converts object to json string for logging purpose.
     * PII data should be obfuscated.
     *
     * @return json string
     */
    @Override
    public String toLogJson() {
        return null;
    }
}
