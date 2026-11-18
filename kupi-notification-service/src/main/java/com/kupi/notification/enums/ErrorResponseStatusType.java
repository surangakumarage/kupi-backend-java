package com.kupi.notification.enums;

import lombok.Getter;

/**
 * Error response status type
 */
@Getter
public enum ErrorResponseStatusType {

    MISSING_REQUIRED_FIELDS(4000, "Required fields are missing"),
    INVALID_TIME_ZONE(4002, "Invalid time zone"),
    INTERNAL_SERVER_ERROR(5000, "Failed due to an internal server error"),
    INVALID_FIREBASE_TOKEN(4020, "Invalid firebase token"),
    INVALID_USER(4004, "Invalid user"),
    INVALID_RECIPIENT(4005, "Invalid recipient's userId"),
    MAX_ID_COUNT(4006, "Max notification id count exceeded"),
    INVALID_REQUEST_FORMAT(4007, "Invalid request format");

    private final int code;
    private final String message;

    ErrorResponseStatusType(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Return String value of code to match with resource-message property key
     *
     * @param code code
     * @return code
     */
    public String getCodeString(int code) {
        return String.valueOf(code);
    }
}
