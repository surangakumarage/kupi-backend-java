package com.kupi.cc.base.exception;

import com.kupi.cc.base.enums.ErrorResponseStatusType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KupiOrderPaymentException extends RuntimeException {
    private static final long serialVersionUID = -5162990231767317301L;

    private final ErrorResponseStatusType errorResponseType;

    /**
     * KupiOrderPaymentException Exception with error message.
     *
     * @param errorMessage error message
     */
    public KupiOrderPaymentException(ErrorResponseStatusType errorResponseType, String errorMessage) {
        super(errorMessage);
        this.errorResponseType = errorResponseType;
    }

    /**
     * KupiOrderPaymentException Exception with error message.
     */
    public KupiOrderPaymentException() {
        this.errorResponseType = ErrorResponseStatusType.INTERNAL_SERVER_ERROR;
    }

    /**
     * KupiOrderPaymentException Exception with error message.
     *
     */
    public KupiOrderPaymentException(ErrorResponseStatusType errorResponseType) {
        this.errorResponseType = errorResponseType;
    }

    /**
     * KupiOrderPaymentException Exception with error message.
     *
     * @param errorMessage error message
     */
    public KupiOrderPaymentException(String errorMessage) {
        super(errorMessage);
        this.errorResponseType = ErrorResponseStatusType.INTERNAL_SERVER_ERROR;
    }

    /**
     * Authentication Exception with error message and throwable error
     *
     * @param errorMessage error message
     * @param error        error
     */
    public KupiOrderPaymentException(String errorMessage, Throwable error) {
        super(errorMessage, error);
        this.errorResponseType = ErrorResponseStatusType.INTERNAL_SERVER_ERROR;
    }

}
