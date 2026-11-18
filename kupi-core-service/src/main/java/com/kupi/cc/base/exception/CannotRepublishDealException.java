package com.kupi.cc.base.exception;

/**
 * Exception thrown when trying to publish an UNPUBLISHED deal again.
 */
public class CannotRepublishDealException extends InvalidDealException {

    /**
     * Constructor with error message.
     *
     * @param errorMessage error message
     */
    public CannotRepublishDealException(String errorMessage) {
        super(errorMessage);
    }
}


