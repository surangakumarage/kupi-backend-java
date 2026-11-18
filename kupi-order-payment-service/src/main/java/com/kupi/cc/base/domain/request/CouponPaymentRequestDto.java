package com.kupi.cc.base.domain.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Coupon payment request dto
 */

@Getter
@Setter
public class CouponPaymentRequestDto extends RequestDto {

    private static final long serialVersionUID = -7761248024487014932L;
    private String dealId;
    private String paymentBrokerId;
    private BigDecimal amount;

    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(dealId) && isNonEmpty(paymentBrokerId) && amount != null;
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}
