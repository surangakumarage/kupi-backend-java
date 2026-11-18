package com.kupi.cc.location.exception;

/**
 * Kupi Location Exception
 */
public class KupiLocationException extends RuntimeException {

    /**
     * KupiLocationException Exception with error message.
     *
     * @param errorMessage error message
     */
    public KupiLocationException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Authentication Exception with error message and throwable error
     *
     * @param errorMessage error message
     * @param error        error
     */
    public KupiLocationException(String errorMessage, Throwable error) {
        super(errorMessage, error);
    }

}
