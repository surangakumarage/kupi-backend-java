package com.kupi.util.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Validation Class
 */
@Component
public class Validator {

    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(?=[A-Za-z0-9.-]*\\.)(?!.*\\.\\.)[A-Za-z0-9.-]+(?<!\\.)$");
    private static final String MOBILE_NO_REGEX = "^\\+(\\d{1,5})\\d{6,14}$";

    /**
     * This method validates a given email.
     *
     * @param email email address
     * @return true/ false
     */
    public boolean isValidEmail(String email) {
        return email != null && email.length() <= 320 && EMAIL.matcher(email).matches();
    }
    /**
     * This method validates a given mobile no.
     *
     * @param mobileNo mobile no
     * @return true/ false
     */
    public boolean isValidMobileNo(String mobileNo) {
        return mobileNo.matches(MOBILE_NO_REGEX);
    }

}
