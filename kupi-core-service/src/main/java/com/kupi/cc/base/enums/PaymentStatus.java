package com.kupi.cc.base.enums;

public enum PaymentStatus {
    SUCCESSFUL("Successful"),
    FAILED("Failed");

    private final String label;

    PaymentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
