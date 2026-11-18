package com.kupi.cc.base.exception;

public class InvalidCreditCardRequestException extends KupiCoreException {
    public InvalidCreditCardRequestException(String errorMessage) {
        super(errorMessage);
    }
}
