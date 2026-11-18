package com.kupi.cc.base.exception;

/**
 * Invalid Deals Of The Day Event Exception
 */
public class InvalidDealsOfTheDayEventException extends KupiCoreException {
    /**
     * BaseComponentException Exception with error message.
     *
     * @param errorMessage error message
     */
    public InvalidDealsOfTheDayEventException(String errorMessage) {
        super(errorMessage);
    }
}
