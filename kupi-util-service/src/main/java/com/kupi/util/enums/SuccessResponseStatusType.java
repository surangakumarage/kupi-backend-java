package com.kupi.util.enums;


import lombok.Getter;

/**
 * Success Response Status Type
 */
@Getter
public enum SuccessResponseStatusType {

    EMAIL_SUCCESS_MESSAGE(2001, "Email sent successfully."),
    SMS_SUCCESS_MESSAGE(2002, "SMS sent successfully."),
    MOBILE_NUMBER_AVAILABLE_ON_WHATSAPP(2003, "WhatsApp availability check successfully.");

    private final int code;
    private final String message;

    SuccessResponseStatusType(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Success code covert into string to read display message from success property file
     *
     * @param successCode successCode
     * @return string code
     */
    public String getCodeString(int successCode) {
        return Integer.toString(successCode);
    }
}
