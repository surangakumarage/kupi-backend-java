package com.kupi.cc.base.exception;

public class StripeSessionNotFoundException extends KupiCoreException{
    public StripeSessionNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
