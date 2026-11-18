package com.kupi.cc.base.exception;

public class InvalidOrderStateException extends KupiOrderPaymentException {

    public InvalidOrderStateException(String errorMessage) {
        super(errorMessage);
    }
}
