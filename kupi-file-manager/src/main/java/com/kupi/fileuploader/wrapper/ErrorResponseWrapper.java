package com.kupi.fileuploader.wrapper;


import com.kupi.fileuploader.domain.response.ResponseDto;
import com.kupi.fileuploader.enums.ErrorResponseStatusType;
import com.kupi.fileuploader.enums.ResponseStatusType;
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
