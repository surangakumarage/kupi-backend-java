package com.kupi.cc.base.enums;

import lombok.Getter;

@Getter
public enum ApprovalStatus {
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED"),
    UNBLOCKED("UNBLOCKED"),
    BLOCKED("BLOCKED"),
    NOT_UPDATED("NOT_UPDATED"),
    ENABLED("ENABLED"),
    DRAFT("DRAFT"),
    DEACTIVATE("DEACTIVATE"),
    UNPUBLISHED("UNPUBLISHED");

    private final String status;

    ApprovalStatus(String status) {
        this.status = status;
    }
}
