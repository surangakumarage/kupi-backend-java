package com.kupi.cc.base.exception;

public class InvalidOrderException extends KupiOrderPaymentException {
    public InvalidOrderException(String errorMessage) {
        super(errorMessage);
    }
}
