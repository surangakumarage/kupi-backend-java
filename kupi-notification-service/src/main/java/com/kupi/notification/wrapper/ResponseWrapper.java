package com.kupi.notification.wrapper;

import com.kupi.notification.domain.BaseDto;
import com.kupi.notification.domain.response.ResponseDto;
import com.kupi.notification.enums.ResponseStatusType;
import lombok.Getter;

/**
 * ResponseWrapper
 */
@Getter
public class ResponseWrapper implements BaseDto {

    private final ResponseStatusType status;
    private final String message;
    private final ResponseDto data;
    private final String displayMessage;

    public ResponseWrapper(ResponseStatusType statusType, String message, ResponseDto data, String displayMessage) {
        this.status = statusType;
        this.message = message;
        this.data = data;
        this.displayMessage = displayMessage;
    }

    /**
     * Convenience constructor for simple message-only responses
     */
    public ResponseWrapper(ResponseStatusType statusType, String message) {
        this.status = statusType;
        this.message = message;
        this.data = null;
        this.displayMessage = message;
    }

    /**
     * Convenience constructor for responses with message and data
     */
    public ResponseWrapper(ResponseStatusType statusType, String message, ResponseDto data) {
        this.status = statusType;
        this.message = message;
        this.data = data;
        this.displayMessage = message;
    }

    /**
     * Convenience constructor for responses with message and string data
     */
    public ResponseWrapper(String message, String displayMessage) {
        this.status = null;
        this.message = message;
        this.data = null;
        this.displayMessage = displayMessage;
    }

    /**
     * Convenience constructor for simple string responses (legacy compatibility)
     */
    public ResponseWrapper(String message) {
        this.status = null;
        this.message = message;
        this.data = null;
        this.displayMessage = message;
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}

