package com.kupi.cc.base.domain.response;

import lombok.Getter;
import lombok.Setter;

import java.text.DecimalFormat;
/**
 * CouponPriceResponseDto
 */
@Setter
@Getter
public class CouponPriceResponseDto  extends ResponseDto {
    private static final DecimalFormat df = new DecimalFormat("0.00");
    private static final String USD = "$";

    private Double couponPrice;
    private String displayCouponPrice;

    public CouponPriceResponseDto(Double deductionAmount) {
        if (deductionAmount != null) {
            this.couponPrice = deductionAmount;
            this.displayCouponPrice = USD + df.format(deductionAmount);
        }
    }
}