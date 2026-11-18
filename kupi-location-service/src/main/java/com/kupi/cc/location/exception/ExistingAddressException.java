package com.kupi.cc.location.exception;

/**
 * Existing Address Exception
 */
public class ExistingAddressException extends KupiLocationException {
    /**
     * ExistingAddressException Exception with error message.
     *
     * @param errorMessage error message.
     */
    public ExistingAddressException(String errorMessage) {
        super(errorMessage);
    }
}
