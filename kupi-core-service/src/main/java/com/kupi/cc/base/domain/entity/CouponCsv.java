package com.kupi.cc.base.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * CouponCsv - For automated coupon generation (expiryDate, couponCount)
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CouponCsv {

    private String expiryDate;
    private Integer couponCount;

    /**
     * Validate that the CSV row has required fields
     */
    public boolean isValid() {
        return expiryDate != null && !expiryDate.trim().isEmpty() && couponCount != null && couponCount > 0;
    }
}
