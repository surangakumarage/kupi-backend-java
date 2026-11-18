package com.kupi.otp.exception;

import com.kupi.otp.service.OtpService;

/**
 * This Exception is used in {@link OtpService}
 */
public class InvalidOtpKeyException extends OTPServiceException {

    /**
     * Invalid otp key exception
     *
     * @param errorMessage error Message
     */
    public InvalidOtpKeyException(String errorMessage) {
        super(errorMessage);
    }
}
