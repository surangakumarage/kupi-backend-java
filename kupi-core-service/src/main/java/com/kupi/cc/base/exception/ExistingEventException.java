package com.kupi.cc.base.exception;

/**
 * Existing Event Exception
 */
public class ExistingEventException extends KupiCoreException {
    /**
     * BaseComponentException Exception with error message.
     *
     * @param errorMessage error message
     */
    public ExistingEventException(String errorMessage) {
        super(errorMessage);
    }
}
