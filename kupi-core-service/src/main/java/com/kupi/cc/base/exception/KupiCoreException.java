package com.kupi.cc.base.exception;

public class KupiCoreException extends RuntimeException {

    /**
     * BaseComponentException Exception with error message.
     *
     * @param errorMessage error message
     */
    public KupiCoreException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Authentication Exception with error message and throwable error
     *
     * @param errorMessage error message
     * @param error        error
     */
    public KupiCoreException(String errorMessage, Throwable error) {
        super(errorMessage, error);
    }

}
