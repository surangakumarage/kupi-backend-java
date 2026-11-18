package com.kupi.cc.auth.exception;

public class SliderImageLimitExceededException extends AuthServiceException {
    public SliderImageLimitExceededException(String errorMessage) {
        super(errorMessage);
    }
}
