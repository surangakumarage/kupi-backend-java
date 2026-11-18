package com.kupi.cc.base.enums;

/**
 * OrderStatus
 */
public enum OrderStatus {
    CREATED("CRTD"),
    UPDATED("UPDT"),    // belong only for state-sequence, order state cannot be UPDATED at any point
    CANCELLED("CNCL");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
