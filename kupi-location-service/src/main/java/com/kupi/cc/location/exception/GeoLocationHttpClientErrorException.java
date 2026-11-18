package com.kupi.cc.location.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

public class GeoLocationHttpClientErrorException extends HttpClientErrorException {
    public final Exception exception;

    public GeoLocationHttpClientErrorException(HttpStatus statusCode, String statusText, Exception e) {
        super(statusCode, statusText);
        this.exception = e;
    }
}
