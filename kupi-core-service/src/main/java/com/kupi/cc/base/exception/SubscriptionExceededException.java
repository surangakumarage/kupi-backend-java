package com.kupi.cc.base.exception;

/**
 * Subscription Exceeded Exception
 */
public class SubscriptionExceededException extends KupiCoreException {
    /**
     * BaseComponentException Exception with error message.
     *
     * @param errorMessage error message
     */
    public SubscriptionExceededException(String errorMessage) {
        super(errorMessage);
    }
}
