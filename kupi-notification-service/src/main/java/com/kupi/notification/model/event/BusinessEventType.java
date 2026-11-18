package com.kupi.notification.model.event;

/**
 * Business event types for SSE real-time notifications
 */
public enum BusinessEventType {
    DEAL_CREATED("Deal Created"),
    DEAL_APPROVED("Deal Approved"),
    DEAL_REJECTED("Deal Rejected"),
    COUPON_REDEEMED("Coupon Redeemed"),
    DEAL_STATUS_CHANGED("Deal Status Changed");

    private final String displayName;

    BusinessEventType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
