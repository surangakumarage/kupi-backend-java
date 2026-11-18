package com.kupi.cc.base.exception;


public class InvalidDealException extends KupiCoreException {

    /**
     * BaseComponentException Exception with error message.
     *
     * @param errorMessage error message
     */
    public InvalidDealException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Authentication Exception with error message and throwable error
     *
     * @param errorMessage error message
     * @param error        error
     */
    public InvalidDealException(String errorMessage, Throwable error) {
        super(errorMessage, error);
    }
}
