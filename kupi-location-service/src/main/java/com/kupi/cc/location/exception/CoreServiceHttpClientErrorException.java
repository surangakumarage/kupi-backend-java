package com.kupi.cc.location.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpClientErrorException;

public class CoreServiceHttpClientErrorException extends HttpClientErrorException {
    public final Exception exception;

    public CoreServiceHttpClientErrorException(HttpStatusCode statusCode, String statusText, Exception e) {
        super(statusCode, statusText);
        this.exception = e;
    }

    // Keep legacy constructor for backward compatibility
    public CoreServiceHttpClientErrorException(HttpStatus statusCode, String statusText, Exception e) {
        super(statusCode, statusText);
        this.exception = e;
    }
}
