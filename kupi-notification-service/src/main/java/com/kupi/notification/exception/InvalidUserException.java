package com.kupi.notification.exception;

/**
 * This exception is Notification Service class
 */
public class InvalidUserException extends NotificationServiceException {

    /**
     * InvalidUser Exception with error message.
     *
     * @param errorMessage error message
     */
    public InvalidUserException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * InvalidUser Exception with error message.
     *
     * @param errorMessage error message
     */
    public InvalidUserException(String errorMessage, Throwable error) {
        super(errorMessage, error);
    }

}
