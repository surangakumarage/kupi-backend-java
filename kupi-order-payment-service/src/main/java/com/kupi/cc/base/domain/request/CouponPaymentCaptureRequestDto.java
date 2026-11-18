package com.kupi.cc.base.domain.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CouponPaymentCaptureRequestDto extends PaymentCaptureRequestDto{

    private static final long serialVersionUID = -4776142133896339890L;
    private String couponId;
    private String dealId;

    public CouponPaymentCaptureRequestDto(String couponId, String dealId) {
        this.couponId = couponId;
        this.dealId = dealId;
    }

    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(couponId) && isNonEmpty(dealId) && super.isRequiredAvailable();
    }
}
