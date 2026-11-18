package com.kupi.cc.base.exception;

/**
 * InvalidUserException
 */
public class InvalidUserException extends KupiCoreException {
    public InvalidUserException(String errorMessage) {
        super(errorMessage);
    }

    public InvalidUserException(String errorMessage, Throwable error) {
        super(errorMessage, error);
    }
}