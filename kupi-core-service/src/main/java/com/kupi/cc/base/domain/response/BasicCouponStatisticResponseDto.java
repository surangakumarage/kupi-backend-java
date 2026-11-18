package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.CouponStatistics;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Basic coupon statistic response Dto
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BasicCouponStatisticResponseDto {

    private String id;
    private int totalCoupons;
    private int redeemedCoupons;
    private int remainingCoupons;
    private boolean couponActive;

    public BasicCouponStatisticResponseDto(CouponStatistics couponStatistics) {
        this.id = couponStatistics.getId();
        this.totalCoupons = couponStatistics.getTotalCoupons();
        this.redeemedCoupons = couponStatistics.getRedeemedCoupons();
        this.remainingCoupons = couponStatistics.getRemainingCoupons();
        this.couponActive = couponStatistics.getDeal().isCouponActive();
    }
}
