package com.kupi.cc.base.enums;

/**
 * PaymentStatus
 */
public enum PaymentStatus {

    ORDER_CREATED("ODR_CRTD"),
    CAPTURED("CAPTURD");

    private final String value;

    PaymentStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
