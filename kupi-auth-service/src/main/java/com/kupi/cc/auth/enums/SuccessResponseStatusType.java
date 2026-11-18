package com.kupi.cc.auth.enums;


import lombok.Getter;

/**
 * Success Response Status Type
 */
@Getter
public enum SuccessResponseStatusType {

    UPDATE_USER(2000, "Successfully updated the user"),
    UPDATE_EMAIL(2001, "Successfully updated email address"),
    UPDATE_MOBILE_NO(2002, "Successfully updated mobile number"),
    CREATE_USER(2003, "Successfully created the user"),
    GET_USER(2004, "Successfully returned user"),
    UPDATE_PASSWORD(2005, "Successfully updated password"),
    RESET_PASSWORD(2006, "Successfully reset the password"),
    LOGGED_IN(2007, "Successfully logged-in the user"),
    LOGGED_OUT(2008, "Successfully logged-out the user"),
    GET_USER_UPDATED_MOBILE_NO(2009, "Success returned updated mobile numbers"),
    VALID_TOKEN(2010, "Valid user token."),
    CREATE_ROLE(2011, "Role created successfully"),
    GET_ALL_PERMISSIONS(2012, "Successfully returned permissions"),
    GET_ROLE(2013, "Successfully returned the role"),
    DELETE_ROLE(2014, "Role removed successfully"),
    UPDATE_ROLE(2015, "Role updated successfully"),
    GET_ROLE_SUMMARY(2016, "Successfully returned the role summary"),
    GET_ROLE_USER_SUMMARY(2017, "Successfully returned the role user summary"),
    UPDATE_ROLE_USER(2018, "Successfully updated the role user"),
    GET_USERS(2019, "Successfully returned users."),
    GET_PENDING_MERCHANTS(2021, "Successfully returned pending merchants."),
    BLOCKED_USER(2022, "Successfully blocked user."),
    APPROVED_USER(2023, "Successfully approved user."),
    GET_MERCHANT(2024, "Successfully returned merchant."),
    UPDATED_BUSINESS_PROFILE(2025, "Successfully updated the business profile."),
    GET_BUSINESS_PROFILE(2026, "Successfully returned the business profile."),
    UPDATED_CONTACT_PROFILE(2027, "Successfully updated the contact profile."),
    GET_CONTACT_PROFILE(2028, "Successfully returned the contact profile."),
    NO_CONTACT_INFO_FOUND(2029, "Add contact information for merchant, " +
            "no existing contact information available."),
    NO_BUSINESS_INFO_FOUND(2030, "Add business information for merchant, " +
            "no existing business information available."),
    GET_PENDING_BUSINESS_INFO(2031, "Successfully returned pending business profile."),
    GET_PENDING_CONTACT_PROFILE(2032, "Successfully returned the pending contact profile."),
    GET_BUSINESS_PROFILE_VIEWS(2033, "Successfully returned business profile views."),
    GET_TODAY_SUMMARY(2034, "Successfully returned today's summary."),
    PASSWORD_RESET_TOKEN(2035, "Password reset token has been sent."),
    OTP_SENT(2100, "OTP has been sent to your mobile number."),
    GET_BANK_LIST(2036, "Successfully returned bank list."),
    USER_DELETED(2037, "Successfully deleted user"),
    UNBLOCKED_USER(2038, "Successfully unblocked user."),
    REJECTED_USER(2039, "Successfully rejected the user."),
    APPROVED_CONTACT_PROFILE(2040, "Successfully approved the contact profile."),
    REJECTED_CONTACT_PROFILE(2041, "Successfully rejected the contact profile."),
    APPROVED_BUSINESS_INFO(2042, "Successfully approved the business profile."),
    REJECTED_BUSINESS_INFO(2043, "Successfully rejected the business profile."),
    ACTIVE_BUSINESS_PROFILES(2044, "Successfully returned active business profiles."),
    SUCCESSFULLY_GENERATED_API_KEY(2045, "Successfully generated api key"),
    SUCCESSFULLY_RETURNED_KEY(2046, "Successfully returned the api key"),
    MOBILE_VERIFIED(2047, "Successfully verified the mobile verified."),
    EMAIL_VERIFIED(2048, "Successfully verified the email verified."),
    UPDATED_PREFERRED_MERCHANTS(2049, "Successfully updated preferred merchants."),
    GET_PREFERRED_MERCHANTS(2050, "Successfully returned preferred merchants."),
    SLIDER_IMAGES_ADDED(2051, "Successfully slider images added."),
    GET_SLIDER_IMAGES(2052, "Successfully returned slider images for merchant."),
    MERCHANT_MENU_IMAGE_SAVED(2053,"Successfully saved merchant menu."),
    GET_MENU_IMAGE(2054,"Successfully returned menu image."),
    SUCCESSFULLY_RETURNED_DEMO_VIDEO_WATCH_STATUS(2055,"Successfully returned the demo video watch status."),
    CREATE_DEMO_VIDEO(2056,"Successfully saved demo video."),
    RETURNED_DEMO_VIDEO(2057,"Successfully returned active demo video.");

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
