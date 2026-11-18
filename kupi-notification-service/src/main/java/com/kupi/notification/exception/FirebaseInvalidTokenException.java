package com.kupi.notification.exception;

public class FirebaseInvalidTokenException extends NotificationServiceException {

    /**
     * FirebaseInvalidToken Exception with error message.
     *
     * @param errorMessage error message
     */
    public FirebaseInvalidTokenException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * FirebaseInvalidToken Exception with error message.
     *
     * @param errorMessage error message
     */
    public FirebaseInvalidTokenException(String errorMessage, Throwable error) {
        super(errorMessage, error);
    }

}
