package com.kupi.cc.base.enums;

import lombok.Getter;

/**
 * CouponRejection enum
 */
@Getter
public enum CouponRejection {

    EXPIRED("Coupon is expired or expiring soon."),
    DUPLICATED("Coupon is duplicated.");

    private final String reason;

    CouponRejection(String message) {
        this.reason = message;
    }
}
