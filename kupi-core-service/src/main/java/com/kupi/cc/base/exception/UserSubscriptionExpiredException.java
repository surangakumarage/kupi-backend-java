package com.kupi.cc.base.exception;

public class UserSubscriptionExpiredException extends RuntimeException {
    public UserSubscriptionExpiredException(String message) {
        super(message);
    }
}
