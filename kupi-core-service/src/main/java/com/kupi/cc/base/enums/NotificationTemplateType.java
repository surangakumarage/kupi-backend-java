package com.kupi.cc.base.enums;

import lombok.Getter;

/**
 * deal approve/reject mail/sms template names
 */
@Getter
public enum NotificationTemplateType {
    APPROVED("SMS-APPROVED-DEAL", "HEADER-APPROVED-DEAL", "BODY-APPROVED-DEAL"),
    REJECTED("SMS-REJECTED-DEAL", "HEADER-REJECTED-DEAL", "BODY-REJECTED-DEAL"),
    RENEW_SUBSCRIPTION("SMS-RENEW-SUBSCRIPTION", "HEADER-RENEW-SUBSCRIPTION", "BODY-RENEW-SUBSCRIPTION");

    private final String sms;
    private final String emailHeader;
    private final String emailBody;

    NotificationTemplateType(String sms, String emailHeader, String emailBody) {
        this.sms = sms;
        this.emailHeader = emailHeader;
        this.emailBody = emailBody;
    }
}