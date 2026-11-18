package com.kupi.cc.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * CouponErrorType
 */
@Getter
@AllArgsConstructor
public enum CouponErrorType {

    INVALID_COUPON_ID(ErrorResponseStatusType.INVALID_COUPON_ID),
    COUPON_NOT_FOUND(ErrorResponseStatusType.COUPON_NOT_FOUND),
    INACTIVE_COUPON(ErrorResponseStatusType.INACTIVE_COUPON),
    REDEEMED_COUPON(ErrorResponseStatusType.REDEEMED_COUPON),
    COUPON_ALREADY_PURCHASED(ErrorResponseStatusType.COUPON_ALREADY_PURCHASED),
    PURCHASED_COUPON_CANT_UPDATED(ErrorResponseStatusType.PURCHASED_COUPON_CANT_UPDATED),
    DEAL_IS_NOT_AVAILABLE(ErrorResponseStatusType.DEAL_IS_NOT_AVAILABLE),
    COUPON_NOT_CLAIMED_BY_USER(ErrorResponseStatusType.COUPON_NOT_CLAIMED_BY_USER);;

    private final ErrorResponseStatusType errorResponseStatusType;

}
