package com.kupi.notification.exception;

public class NotificationServiceException extends RuntimeException {

    /**
     * BaseComponentException Exception with error message.
     *
     * @param errorMessage error message
     */
    public NotificationServiceException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Authentication Exception with error message and throwable error
     *
     * @param errorMessage error message
     * @param error        error
     */
    public NotificationServiceException(String errorMessage, Throwable error) {
        super(errorMessage, error);
    }

}
