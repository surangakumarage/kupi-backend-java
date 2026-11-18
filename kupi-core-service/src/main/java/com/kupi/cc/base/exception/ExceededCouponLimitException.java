package com.kupi.cc.base.exception;

public class ExceededCouponLimitException extends KupiCoreException{
    public ExceededCouponLimitException(String errorMessage) {
        super(errorMessage);
    }
}
