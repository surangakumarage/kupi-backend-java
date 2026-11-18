package com.kupi.cc.location.exception;

/**
 * Existing Name Exception
 */
public class ExistingNameException extends KupiLocationException {
    /**
     * ExistingNameException Exception with error message.
     *
     * @param errorMessage error message.
     */
    public ExistingNameException(String errorMessage) {
        super(errorMessage);
    }
}