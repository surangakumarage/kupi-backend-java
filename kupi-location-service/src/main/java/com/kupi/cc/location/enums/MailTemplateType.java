package com.kupi.cc.location.enums;

import lombok.Getter;

@Getter
public enum MailTemplateType {
    APPROVED( "APPROVED-BRANCH-SUBJECT", "APPROVED-BRANCH-BODY"),
    REJECTED( "REJECTED-BRANCH-SUBJECT", "REJECTED-BRANCH-BODY");

    private final String emailHeader;
    private final String emailBody;

    MailTemplateType(String emailHeader, String emailBody) {
        this.emailHeader = emailHeader;
        this.emailBody = emailBody;
    }
}