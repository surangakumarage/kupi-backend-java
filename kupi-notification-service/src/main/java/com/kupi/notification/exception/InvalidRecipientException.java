package com.kupi.notification.exception;

/**
 * This exception is Notification Service class
 */
public class InvalidRecipientException extends NotificationServiceException {

    /**
     * InvalidUser Exception with error message.
     *
     * @param errorMessage error message
     */
    public InvalidRecipientException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * InvalidUser Exception with error message.
     *
     * @param errorMessage error message
     */
    public InvalidRecipientException(String errorMessage, Throwable error) {
        super(errorMessage, error);
    }

}
