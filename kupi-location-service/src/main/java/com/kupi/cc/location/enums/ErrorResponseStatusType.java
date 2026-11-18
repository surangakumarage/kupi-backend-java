package com.kupi.cc.location.enums;

import lombok.Getter;

/**
 * Error response Status Type
 */
@Getter
public enum ErrorResponseStatusType {

    INTERNAL_SERVER_ERROR(5000, "Internal server error."),
    MISSING_REQUIRED_FIELDS(4000, "Required fields are missing"),
    EXISTING_BRANCH_NAME(4001, "Branch name is already existing for the user."),
    INVALID_BRANCH_ID(4002, "Invalid branchId."),
    INVALID_USER_ID(4003, "Invalid userId."),
    INVALID_MAP_URL(4004, "Invalid map URL."),
    INVALID_LATITUDE(4005, "Invalid latitude."),
    INVALID_LONGITUDE(4006, "Invalid longitude."),
    UNSUPPORTED_USER_TYPE_ACTION(4007, "Unsupported user type for the action."),
    EXISTING_BRANCH_ADDRESS(4008, "Branch address is already existing."),
    BUSINESS_PROFILE_NOT_FOUND(4009, "Business profile not found for user."),
    BUSINESS_PROFILE_PENDING_NOT_APPROVED(4010,"Business profile pending is not approved.");

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