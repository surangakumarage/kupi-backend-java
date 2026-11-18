package com.kupi.cc.auth.exception;

public class InvalidTokenException extends AuthServiceException {

    public InvalidTokenException(String errorMessage) {
        super(errorMessage);
    }
}
