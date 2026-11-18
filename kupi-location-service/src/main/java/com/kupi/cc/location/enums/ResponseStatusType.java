package com.kupi.cc.location.enums;

/**
 * Enum values for response
 */
public enum ResponseStatusType {

    SUCCESS("Success"),
    ERROR("Error");

    private String status;

    ResponseStatusType(String status) {
        this.status = status;
    }

}
