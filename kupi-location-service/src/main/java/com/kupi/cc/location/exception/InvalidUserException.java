package com.kupi.cc.location.exception;

/**
 * Invalid User Exception
 */
public class InvalidUserException extends KupiLocationException {
    /**
     * InvalidUserException Exception with error message.
     *
     * @param errorMessage error message.
     */
    public InvalidUserException(String errorMessage) {
        super(errorMessage);
    }
}