package com.kupi.cc.base.exception;

import com.kupi.cc.base.enums.LimitExceededType;
import lombok.Getter;

/**
 * Limit Exceeded Exception
 */
@Getter
public class LimitExceededException extends KupiCoreException {

    private final LimitExceededType limitExceededType;

    /**
     * BaseComponentException Exception with error message.
     *
     * @param errorMessage error message
     */
    public LimitExceededException(String errorMessage, LimitExceededType limitExceededType) {
        super(errorMessage);
        this.limitExceededType = limitExceededType;
    }
}
