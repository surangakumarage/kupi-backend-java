package com.kupi.exception;

/**
 * Gateway Server Exception
 * Generic exception for API Gateway errors (formerly ZuulServerException)
 * Works with Spring Cloud Gateway
 */
public class GatewayServerException extends RuntimeException {

    /**
     * GatewayServerException with error message.
     *
     * @param errorMessage error message
     */
    public GatewayServerException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * GatewayServerException with error message and throwable error
     *
     * @param errorMessage error message
     * @param error        error
     */
    public GatewayServerException(String errorMessage, Throwable error) {
        super(errorMessage, error);
    }
}
