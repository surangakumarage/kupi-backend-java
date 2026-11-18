package com.kupi.cc.base.domain.request;

import lombok.Getter;
import lombok.Setter;

/**
 * Redeem RequestDto
 */
@Getter
@Setter
public class RedeemRequestDto extends RequestDto {

    private String merchantId;
    private String couponCode;

    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(merchantId) && isNonEmpty(couponCode);
    }

    public boolean isRequiredAvailableForCouponValidation() {
        return isNonEmpty(merchantId) && isNonEmpty(couponCode);
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}
