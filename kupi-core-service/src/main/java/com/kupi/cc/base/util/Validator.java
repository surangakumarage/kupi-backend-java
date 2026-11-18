package com.kupi.cc.base.util;

import org.springframework.stereotype.Component;

/**
 * This class is used to validate fields.
 */
@Component
public class Validator {


    private static final String EMAIL_REGEX =
            "^[A-Za-z0-9._%+-]+@(?:[A-Za-z0-9-]{1,63}\\.){1,10}[A-Za-z]{2,63}$";
    private static final String MOBILE_NO_REGEX = "^\\+(\\d{2}[-])\\d{9}$";
    private static final String NIC_REGEX = "^(?:(?:19|20)?\\d{2}[0-9]{10}|[0-9]{9}[xXvV])$";

    public boolean isValidEmail(String email) {
        return email != null && !email.isBlank() && email.trim().matches(EMAIL_REGEX);
    }

    public boolean isValidMobileNoWithCountryCode(String mobileNo) {
        return mobileNo != null && !mobileNo.isBlank() && mobileNo.matches(MOBILE_NO_REGEX);
    }

    public boolean isValidNIC(String nic) {
        return nic != null && !nic.isBlank() && nic.trim().matches(NIC_REGEX);
    }
}
