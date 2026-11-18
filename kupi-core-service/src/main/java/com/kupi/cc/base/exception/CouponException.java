package com.kupi.cc.base.exception;

import com.kupi.cc.base.enums.CouponErrorType;
import lombok.Getter;

/**
 * CouponException
 */
@Getter
public class CouponException extends KupiCoreException {

    private final CouponErrorType couponErrorType;

    public CouponException(String errorMessage, CouponErrorType couponErrorType) {
        super(errorMessage);
        this.couponErrorType = couponErrorType;
    }
}
