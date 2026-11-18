package com.kupi.otp.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Validation Class
 */
@Component
public class Validator {

    private static final Pattern EMAIL = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@(?=[A-Za-z0-9.-]*\\.)(?!.*\\.\\.)[A-Za-z0-9.-]+(?<!\\.)$"
    );
    private static final String MOBILE_NO_REGEX = "^\\+(\\d{1,5})[-]\\d{6,14}$";
    private static final String OTP_REGEX = "^([0-9]{6})$";

    /**
     * This method validates a given email.
     *
     * @param email email address
     * @return true/ false
     */
    public boolean isValidEmail(String email) {
        return email != null && EMAIL.matcher(email).matches();
    }

    /**
     * This method validates a given mobile no.
     *
     * @param mobileNo mobile no
     * @return true/ false
     */
    public boolean isValidMobileNo(String mobileNo) {
        return true;
    }

    /**
     * This method validates a given otp
     *
     * @param otp otp
     * @return true/ false
     */
    public boolean isValidOtp(String otp) {
        return otp.matches(OTP_REGEX);
    }

}
