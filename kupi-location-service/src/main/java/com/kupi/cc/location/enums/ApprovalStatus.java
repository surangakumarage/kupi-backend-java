package com.kupi.cc.location.enums;

import lombok.Getter;

/**
 * Approval status types
 */
@Getter
public enum ApprovalStatus {
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED");

    private final String message;

    ApprovalStatus(String message) {
        this.message = message;
    }
}
