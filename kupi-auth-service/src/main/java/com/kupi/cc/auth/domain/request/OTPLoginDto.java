package com.kupi.cc.auth.domain.request;

import lombok.Getter;
import lombok.Setter;

/**
 * OTP Login Request DTO
 */
@Getter
@Setter
public class OTPLoginDto extends RequestDto {

    public static final String TOKEN_KEY = "token";
    public static final String MOBILE_NO_KEY = "mobile no";

    private String token;
    private MobileNoRequestDto mobileNo;

    /**
     * Check all required fields are available.
     *
     * @return true if all required fields are present, false otherwise
     */
    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(token) &&
               mobileNo != null && mobileNo.isRequiredAvailable();
    }

    /**
     * Convert object to JSON string for logging (obfuscated for security).
     *
     * @return JSON string representation
     */
    @Override
    public String toLogJson() {
        return toJson();
    }
}
