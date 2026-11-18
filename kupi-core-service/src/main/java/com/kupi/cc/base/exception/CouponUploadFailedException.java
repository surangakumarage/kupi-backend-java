package com.kupi.cc.base.exception;

/**
 * CouponUploadFailedException
 */
public class CouponUploadFailedException extends KupiCoreException {

    /**
     * CouponUploadFailedException Exception with error message.
     *
     * @param errorMessage error message
     */
    public CouponUploadFailedException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * CouponUploadFailedException Exception with error message and error.
     *
     * @param errorMessage errorMessage
     * @param error        error
     */
    public CouponUploadFailedException(String errorMessage, Throwable error) {
        super(errorMessage, error);
    }
}
