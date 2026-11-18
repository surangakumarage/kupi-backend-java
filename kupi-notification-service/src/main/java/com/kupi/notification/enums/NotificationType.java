package com.kupi.notification.enums;

import lombok.Getter;

/**
 * Notification Type
 */
@Getter
public enum NotificationType {

    DEAL("DEAL"),
    MERCHANT("MERCHANT"),
    BANK("BANK"),
    MERCHANT_DEAL("MERCHANT-DEAL"),
    BANK_DEAL("BANK-DEAL");

    private final String typeValue;

    NotificationType(String typeValue) {
        this.typeValue = typeValue;
    }
}
