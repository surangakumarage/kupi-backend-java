package com.kupi.cc.base.exception;

public class InvalidCouponException extends KupiOrderPaymentException {

    public InvalidCouponException(String errorMessage) {
        super(errorMessage);
    }
}
