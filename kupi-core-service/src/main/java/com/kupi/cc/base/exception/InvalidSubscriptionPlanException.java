package com.kupi.cc.base.exception;

/**
 * Invalid Subscription Plan Exception
 */
public class InvalidSubscriptionPlanException extends KupiCoreException {
    public InvalidSubscriptionPlanException(String errorMessage) {
        super(errorMessage);
    }
}