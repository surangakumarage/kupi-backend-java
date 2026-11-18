package com.kupi.cc.auth.domain.request;

import com.kupi.cc.auth.enums.RoleType;
import lombok.Getter;
import lombok.Setter;

/**
 * OTP User Registration Request DTO
 */
@Getter
@Setter
public class OTPUserRegistrationDto extends RequestDto {

    public static final String TOKEN_KEY = "token";
    public static final String MOBILE_NO_KEY = "mobile no";
    public static final String FULL_NAME_KEY = "full name";
    public static final String EMAIL_KEY = "email";
    public static final String PASS_KEY = "password";
    public static final String LANGUAGE_KEY = "language";

    private String token;
    private MobileNoRequestDto mobileNo;
    private String fullName;
    private String email;
    private String password;
    private String language = "English";
    private RoleType roleType = RoleType.USER;

    /**
     * Check all required fields are available.
     *
     * @return true if all required fields are present, false otherwise
     */
    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(token) &&
               mobileNo != null && mobileNo.isRequiredAvailable() &&
               isNonEmpty(fullName) &&
               roleType != RoleType.ADMIN;
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
