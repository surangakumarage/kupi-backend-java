package com.kupi.cc.location.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpClientErrorException;

public class NotificationServiceClientErrorException extends HttpClientErrorException {
    public final Exception exception;

    public NotificationServiceClientErrorException(HttpStatusCode statusCode, String statusText, Exception e) {
        super(statusCode, statusText);
        this.exception = e;
    }

    // Keep legacy constructor for backward compatibility
    public NotificationServiceClientErrorException(HttpStatus statusCode, String statusText, Exception e) {
        super(statusCode, statusText);
        this.exception = e;
    }
}
