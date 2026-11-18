package com.kupi.cc.location.exception;

/**
 * Invalid Branch Exception
 */
public class InvalidBranchException extends KupiLocationException {
    /**
     * InvalidBranchException Exception with error message.
     *
     * @param errorMessage error message.
     */
    public InvalidBranchException(String errorMessage) {
        super(errorMessage);
    }
}