package com.kupi.notification.enums;


import lombok.Getter;

/**
 * Success Response Status Type
 */
@Getter
public enum SuccessResponseStatusType {

    UPDATE_USER(2000, "Successfully updated"),
    ADD_TOKEN(2021, "Successfully added the token"),
    SEND_NOTIFICATION(2022, "Successfully sent the notification to the recipient"),
    MARK_READ(2023, "Successfully marked notifications as read."),
    NOTIFICATION_SUMMARY(2024, "Successfully returned the notification summary."),
    UNREAD_NOTIFICATION_COUNT(2025, "Successfully returned the unread notification count."),
    DELETE_NOTIFICATIONS(2026, "Successfully deleted notifications."),
    NOTIFICATIONS_ENABLED(2027, "Notifications enabled."),
    NOTIFICATIONS_DISABLED(2028, "Notifications disabled."),
    NOTIFICATION_STATUS(2029, "Successfully returned notification status."),
    REMOVE_TOKEN(2030, "Successfully removed the token."),
    HEARTBEAT_RECEIVED(2031, "Heartbeat received and connection maintained."),
    SUCCESS(2032, "Operation successful.");

    private final String code;
    private final String message;

    SuccessResponseStatusType(int code, String message) {
        this.code = getCodeString(code);
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
