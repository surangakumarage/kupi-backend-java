package com.kupi.cc.base.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Coupon statistics entity
 */
@Entity
@Table(name = "coupon_statistics")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CouponStatistics {
    @Transient
    private static final String COUPON_STATISTICS_ID_PREFIX = "cousid-";

    @Id
    private String id;
    private int totalCoupons;
    private int redeemedCoupons;
    private int remainingCoupons;
    @OneToOne(cascade = CascadeType.ALL)
    private Deal deal;
    private String merchantId;
    private long createdAt;
    private long updatedAt;

    public CouponStatistics(int totalCoupons, Deal deal) {
        this.id = COUPON_STATISTICS_ID_PREFIX + UUID.randomUUID();
        this.totalCoupons = totalCoupons;
        this.remainingCoupons = totalCoupons;
        this.deal = deal;
        this.merchantId = deal.getMerchantId();
        this.createdAt = System.currentTimeMillis();
    }

    /**
     * This method updates coupon statistics when coupon is purchased/claimed by user.
     * Only remaining coupon count should be decreased.
     */
    public void updateCouponStatisticsOnPurchase() {
        remainingCoupons--;
        updatedAt = System.currentTimeMillis();
    }

    /**
     * This method updates coupon statistics when coupon purchase is cancelled.
     */
    public void updateCouponStatisticsOnPurchaseCancellation() {
        remainingCoupons++;
        updatedAt = System.currentTimeMillis();
    }

    /**
     * This method updates coupon statistics when coupon is redeemed by merchant.
     * Only redeemed coupon count should be increased.
     */
    public void updateCouponStatisticsOnRedeem() {
        redeemedCoupons++;
        updatedAt = System.currentTimeMillis();
    }
}
